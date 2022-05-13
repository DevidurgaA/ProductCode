package com.tlc.tools.sql;

import java.io.File;
import java.util.Objects;

/**
 * @author Abishek
 * @version 1.0
 */
public record MetaFile(File file, File targetDir, String packageName)
{
    public MetaFile(File file, File targetDir, String packageName)
    {
        this.file = Objects.requireNonNull(file);
        this.targetDir = Objects.requireNonNull(targetDir);
        this.packageName = Objects.requireNonNull(packageName);
    }

    @Override
    public File file()
    {
        return file;
    }

    @Override
    public File targetDir()
    {
        return targetDir;
    }

    @Override
    public String packageName()
    {
        return packageName;
    }
}
