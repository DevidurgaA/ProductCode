package com.tlc.sql.internal.parser;

import com.tlc.commons.code.ErrorCode;
import com.tlc.sql.internal.status.SQLErrorCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;


/**
 * @author Abishek
 * @version 1.0
 */
public class MetaDataUtil
{
    private static final Logger LOGGER = LoggerFactory.getLogger(MetaDataUtil.class);
    public static List<File> getFileListFromConfFile(String confFilePath, String tagName) throws IOException, SAXException, ParserConfigurationException
    {
        final File metaConf = new File(confFilePath);

        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder builder = factory.newDocumentBuilder();
        final Document doc = builder.parse(metaConf);
        final Element rootElement = doc.getDocumentElement();

        final NodeList metaFilesList = rootElement.getElementsByTagName(tagName);
        final int totalFiles = metaFilesList.getLength();
        if(totalFiles > 0)
        {
            final List<File> confFiles = new LinkedList<>();
            final String directory = metaConf.getParent() + File.separator;
            for(int index = 0 ; index < totalFiles ; index++)
            {
                final Element metaFileElement = (Element) metaFilesList.item(index);
                final String path = metaFileElement.getAttribute(MetaTagConstants.META_FILE_PATH);
                if(path.isEmpty())
                {
                    LOGGER.error("Invalid meta file, path cannot be null");
                    throw ErrorCode.get(SQLErrorCodes.DB_SCHEMA_INVALID_FILE);
                }
                final File metaFile = new File(directory + path);
                if(!metaFile.exists() || !metaFile.isFile())
                {
                    LOGGER.error("Invalid meta file or file not exists, file {}", metaFile.getAbsolutePath());
                    throw ErrorCode.get(SQLErrorCodes.DB_SCHEMA_INVALID_FILE);
                }
                else
                {
                    confFiles.add(metaFile);
                }
            }
            return confFiles;
        }
        else
        {
            LOGGER.error("Invalid meta file or no valid entry found, file {}", confFilePath);
            throw ErrorCode.get(SQLErrorCodes.DB_SCHEMA_INVALID_FILE);
        }
    }
}
