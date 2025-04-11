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
import org.xmpp.forms.DataForm;
import org.xmpp.forms.FormField;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Implementation of the "Set Message of the Day" ad-hoc command as defined in XEP-0133: Service Administration.
 *
 * @author Guus der Kinderen, guus@goodbytes.nl
 * @see <a href="https://xmpp.org/extensions/xep-0133.html#set-motd">XEP-0133: Service Administration</a>
 */
public class SetMotD extends AdHocCommand
{
    @Override
    public String getCode()
    {
        return "http://jabber.org/protocol/admin#set-motd";
    }

    @Override
    public String getDefaultLabel()
    {
        return LocaleUtils.getLocalizedPluginString("motd", "commands.admin.motd.setmotd.label");
    }

    @Override
    public int getMaxStages(SessionData data)
    {
        return 1;
    }

    @Override
    public void execute(SessionData sessionData, Element command)
    {
        final Locale preferredLocale = SessionManager.getInstance().getLocaleForSession(sessionData.getOwner());

        Element note = command.addElement("note");

        Map<String, List<String>> data = sessionData.getData();

        boolean requestError = false;
        final MotDPlugin plugin = (MotDPlugin) XMPPServer.getInstance().getPluginManager().getPluginByName("MotD (Message of the Day)").orElseThrow();
        if (plugin.isEnabled()) {
            note.addAttribute( "type", "error" );
            note.setText("A message of the day is already set. If you intend to edit the message of the day, use the 'Edit Message of the Day' command instead.");
            requestError = true;
        }

        final List<String> motd = data.get("motd");
        if (motd == null || motd.isEmpty()) {
            note.addAttribute( "type", "error" );
            note.setText("Please provide text for the message of the day. If you intend to remove the message of the day, use the 'Delete Message of the Day' command instead.");
            requestError = true;
        }

        if ( requestError )
        {
            // We've collected all errors. Return without applying changes.
            return;
        }

        // No errors.
        plugin.setMessage(String.join("\r\n", motd));
        plugin.setEnabled(true);

        // Answer that the operation was successful
        note.addAttribute("type", "info");
        note.setText(LocaleUtils.getLocalizedString("commands.global.operation.finished.success", preferredLocale));
    }

    @Override
    protected void addStageInformation(SessionData data, Element command)
    {
        DataForm form = new DataForm(DataForm.Type.form);
        form.setTitle("Setting the Message of the Day");
        form.addInstruction("Fill out this form to set the message of the day.");

        FormField field = form.addField();
        field.setType(FormField.Type.hidden);
        field.setVariable("FORM_TYPE");
        field.addValue("http://jabber.org/protocol/admin");

        field = form.addField();
        field.setType(FormField.Type.text_multi);
        field.setLabel("Message of the Day");
        field.setVariable("motd");

        // Add the form to the command
        command.add(form.getElement());
    }

    @Override
    protected List<Action> getActions(SessionData data)
    {
        return Collections.singletonList(Action.complete);
    }

    @Override
    protected Action getExecuteAction(SessionData data)
    {
        return AdHocCommand.Action.complete;
    }
}
