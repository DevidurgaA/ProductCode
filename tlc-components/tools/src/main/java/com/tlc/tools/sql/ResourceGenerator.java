package com.tlc.tools.sql;

import com.tlc.sql.api.meta.ColumnDefinition;
import com.tlc.sql.api.meta.TableDefinition;
import com.tlc.sql.internal.parser.MetaDataParser;
import com.tlc.sql.internal.parser.TableDefinitionLoader;
import org.apache.commons.io.FilenameUtils;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Abishek
 * @version 1.0
 */
public class ResourceGenerator
{
	private static final Logger LOGGER = Logger.getLogger("ResourceGenerator");
	private static final MavenXpp3Reader MAVEN_XPP_3_READER = new MavenXpp3Reader();

	static
	{
		System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tH:%1$tM:%1$tS %4$s %5$s%6$s%n");
	}

	public static void main(String[] str) throws Exception
	{
		final long time = System.currentTimeMillis();
		final File rootDir = new File(str.length == 0 ? "." : str[0]);

		final Set<MetaFile> metaFiles = new HashSet<>();
		discoverMetaFiles(rootDir, metaFiles);
		LOGGER.log(Level.INFO, "Discovered Meta Files : {0}", metaFiles);

		final AtomicInteger counter = new AtomicInteger();
		for (MetaFile metaFile : metaFiles)
		{
			final File file = metaFile.file();
			final Map<String, TableDefinition> tableDefinitions = new HashMap<>();
			final TableDefinitionLoader definitionLoader = new TableDefinitionLoader()
			{
				@Override
				public boolean isDuplicate(String tableName)
				{
					return tableDefinitions.containsKey(tableName);
				}

				@Override
				public void add(TableDefinition tableDefinition)
				{
					tableDefinitions.put(tableDefinition.getName(), tableDefinition);
				}

				@Override
				public TableDefinition resolve(String tableName)
				{
					return tableDefinitions.get(tableName);
				}

				@Override
				public boolean loadKeys()
				{
					return false;
				}
			};
			try(FileInputStream inputStream = new FileInputStream(file))
			{
				MetaDataParser.processMetaData(inputStream, definitionLoader);
			}
			final File targetDir = metaFile.targetDir();
			cleanJavaResourceFiles(targetDir);

			final String defaultPackage = metaFile.packageName();
			for (TableDefinition tableDefinition : tableDefinitions.values())
			{
				processTableDefinition(tableDefinition, defaultPackage, targetDir);
			}
			counter.addAndGet(tableDefinitions.size());
		}
		LOGGER.log(Level.INFO, "Total Tables Processed : {0}", counter.get());
		LOGGER.log(Level.INFO, "Total Time taken : "+ (System.currentTimeMillis() - time));
	}

	private static void cleanJavaResourceFiles(File directory)
	{
		final File[] files = directory.listFiles(file -> FilenameUtils.getExtension(file.getName()).equals("java"));
		if(files != null)
		{
			for (File file : files)
			{
				if(!file.delete())
				{
					LOGGER.log(Level.SEVERE, "Unable to delete : "+file.getAbsolutePath());
				}
				else
				{
					LOGGER.log(Level.WARNING,"Successfully deleted : "+file.getAbsolutePath());
				}
			}
		}
	}

	private static void discoverMetaFiles(File directory, Set<MetaFile> metaFiles) throws IOException, XmlPullParserException
	{
		final File pomFile = new File(directory, "pom.xml");
		if(pomFile.exists())
		{
			final Model model = MAVEN_XPP_3_READER.read(new FileReader(pomFile));
			final String packaging = model.getPackaging();
			final String moduleName = model.getArtifactId();
			switch (packaging)
			{
				case "bundle" -> {
					LOGGER.log(Level.FINE,"Bundle discovered : {0} ", moduleName);
					final File file = new File(directory, "src/main/resources/sql/meta.xml");
					if(file.exists())
					{
						final String groupId = Objects.requireNonNullElseGet(model.getGroupId(), () -> model.getParent().getGroupId());
						LOGGER.log(Level.INFO,"Found Meta File in module : {0} ", moduleName);

						final StringBuilder packageBuilder = new StringBuilder(groupId).append(".").append(model.getArtifactId());
						if (!model.getArtifactId().equals("sql"))
						{
							packageBuilder.append(".sql.resource");
						}
						else
						{
							packageBuilder.append(".resource");
						}
						final String packageName = packageBuilder.toString();
						final String packagePath = "src/main/java/" + packageName.replace(".", "/");
						final File targetDir = new File(directory, packagePath);
						metaFiles.add(new MetaFile(file, targetDir, packageName));
					}
					else
					{
						LOGGER.log(Level.FINE,"Meta File not found in module : {0} ", moduleName);
					}
				}
				case "pom" -> {
					LOGGER.log(Level.FINE,"Pom discovered in module : {0} ", moduleName);
					final List<String> modules =  model.getModules();
					LOGGER.log(Level.FINE,"Discovered Child Modules : {0} ", modules.toString());
					for (String module : modules)
					{
						final File childModule = new File(directory, module);
						if(childModule.exists())
						{
							LOGGER.log(Level.INFO,"Processing module : {0} ", module);
							discoverMetaFiles(childModule, metaFiles);
						}
						else
						{
							LOGGER.log(Level.WARNING,"Module not found : {0} ", module);
						}
					}
				}
			}
		}
	}

	private static void processTableDefinition(TableDefinition tableDefinition, String packageName, File targetDir) throws IOException
	{
		final String classContent = getClassContent(packageName, tableDefinition);
		if(targetDir.exists() || targetDir.mkdirs())
		{
			final File destination = new File(targetDir, tableDefinition.getName().toUpperCase() + ".java");
			try(FileWriter writer = new FileWriter(destination))
			{
				writer.write(classContent);
			}
		}
		else
		{
			LOGGER.log(Level.SEVERE,"Unable to create target directory  : {0} ", targetDir.getCanonicalPath());
		}
	}

	private static String getClassContent(String packageName, TableDefinition tableDefinition)
	{
		final String tableName = tableDefinition.getName();
		final StringBuilder source = new StringBuilder().append("package ").append(packageName).append(";\n\n")
				.append("""
						/**
						 * @author Auto Generated
						 */
						""")
				.append("public class ").append(tableName.toUpperCase()).append("\n{\n");
		source.append("\tpublic static final String TABLE = \"").append(tableName).append("\";\n\n");
		final List<ColumnDefinition> columnDefinitions = new ArrayList<>(tableDefinition.getColumnDefinition().values());
		columnDefinitions.sort(Comparator.comparing(ColumnDefinition::getColumnName));
		for (ColumnDefinition columnDef : columnDefinitions)
		{
			final String columnName = columnDef.getColumnName();
			source.append("\t").append("public static final String ").append(columnName.toUpperCase()).append(" = \"").append(columnName).append("\";\n");
		}
		source.append("}");
		return source.toString();
	}

}
