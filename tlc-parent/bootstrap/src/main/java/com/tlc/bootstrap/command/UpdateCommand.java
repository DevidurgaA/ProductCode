package com.tlc.bootstrap.command;

import com.tlc.bootstrap.update.UpdateManager;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;


@Service
@Command(scope = "tlc", name = "update", description = "Server update command")
public class UpdateCommand implements Action
{
    @Reference
    private UpdateManager updateManager;

    @Override
    public Object execute() throws Exception
    {
        updateManager.update();
        return null;
    }
}
