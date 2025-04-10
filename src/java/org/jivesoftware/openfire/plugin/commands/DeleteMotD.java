/*
 * Copyright (C) 2025 Ignite Realtime Foundation. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jivesoftware.openfire.plugin.commands;

import org.dom4j.Element;
import org.jivesoftware.openfire.SessionManager;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.commands.AdHocCommand;
import org.jivesoftware.openfire.commands.SessionData;
import org.jivesoftware.openfire.plugin.MotDPlugin;
import org.jivesoftware.util.LocaleUtils;

import java.util.List;
import java.util.Locale;

/**
 * Implementation of the "Delete Message of the Day" ad-hoc command as defined in XEP-0133: Service Administration.
 *
 * @author Guus der Kinderen, guus@goodbytes.nl
 * @see <a href="https://xmpp.org/extensions/xep-0133.html#delete-motd">XEP-0133: Service Administration</a>
 */
public class DeleteMotD extends AdHocCommand
{
    @Override
    public String getCode()
    {
        return "http://jabber.org/protocol/admin#delete-motd";
    }

    @Override
    public String getDefaultLabel()
    {
        return LocaleUtils.getLocalizedPluginString("motd", "commands.admin.motd.deletemotd.label");
    }

    @Override
    public int getMaxStages(SessionData data)
    {
        return 0;
    }

    @Override
    public void execute(SessionData sessionData, Element command)
    {
        final Locale preferredLocale = SessionManager.getInstance().getLocaleForSession(sessionData.getOwner());

        Element note = command.addElement("note");

        boolean requestError = false;
        final MotDPlugin plugin = (MotDPlugin) XMPPServer.getInstance().getPluginManager().getPluginByName("MotD (Message of the Day)").orElseThrow();
        if (!plugin.isEnabled()) {
            note.addAttribute( "type", "error" );
            note.setText("A message of the day does not exist. You cannot delete it.");
            requestError = true;
        }

        if ( requestError )
        {
            // We've collected all errors. Return without applying changes.
            return;
        }

        // No errors.
        plugin.setEnabled(false);

        // Answer that the operation was successful
        note.addAttribute("type", "info");
        note.setText(LocaleUtils.getLocalizedString("commands.global.operation.finished.success", preferredLocale));
    }

    @Override
    protected void addStageInformation(SessionData data, Element command)
    {
        // Do nothing since there are no stages
    }

    @Override
    protected List<Action> getActions(SessionData data)
    {
        //Do nothing since there are no stages
        return null;
    }

    @Override
    protected Action getExecuteAction(SessionData data)
    {
        //Do nothing since there are no stages
        return null;
    }
}
