/*
 * Copyright (C) 2007-2017 Jive Software, 2017-2025 Ignite Realtime Foundation. All rights reserved.
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

package org.jivesoftware.openfire.plugin;

import java.io.File;
import java.time.Duration;
import java.util.TimerTask;

import org.jivesoftware.openfire.plugin.commands.DeleteMotD;
import org.jivesoftware.openfire.plugin.commands.EditMotD;
import org.jivesoftware.openfire.plugin.commands.SetMotD;
import org.jivesoftware.util.JiveGlobals;
import org.jivesoftware.util.TaskEngine;
import org.jivesoftware.openfire.MessageRouter;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.event.SessionEventDispatcher;
import org.jivesoftware.openfire.event.SessionEventListener;
import org.jivesoftware.openfire.session.Session;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;

/**
 * MotD (Message of the Day) plugin.
 * 
 * @author <a href="mailto:ryan@version2software.com">Ryan Graham</a>
 */
public class MotDPlugin implements Plugin {
   private static final String SUBJECT = "plugin.motd.subject";
   private static final String MESSAGE = "plugin.motd.message";
   private static final String ENABLED = "plugin.motd.enabled";

   private JID serverAddress;
   private MessageRouter router;

   private MotDSessionEventListener listener = new MotDSessionEventListener();

   private final SetMotD setMotD = new SetMotD();
   private final EditMotD editMotD = new EditMotD();
   private final DeleteMotD deleteMotD = new DeleteMotD();

   public void initializePlugin(PluginManager manager, File pluginDirectory) {
      serverAddress = new JID(XMPPServer.getInstance().getServerInfo().getXMPPDomain());
      router = XMPPServer.getInstance().getMessageRouter();

      XMPPServer.getInstance().getAdHocCommandHandler().addCommand(setMotD);
      XMPPServer.getInstance().getAdHocCommandHandler().addCommand(editMotD);
      XMPPServer.getInstance().getAdHocCommandHandler().addCommand(deleteMotD);

      SessionEventDispatcher.addListener(listener);
   }

   public void destroyPlugin() {
      SessionEventDispatcher.removeListener(listener);
      XMPPServer.getInstance().getAdHocCommandHandler().removeCommand(setMotD);
      XMPPServer.getInstance().getAdHocCommandHandler().removeCommand(editMotD);
      XMPPServer.getInstance().getAdHocCommandHandler().removeCommand(deleteMotD);

      listener = null;
      serverAddress = null;
      router = null;
   }

   public void setSubject(String message) {
      JiveGlobals.setProperty(SUBJECT, message);
   }

   public String getSubject() {
      return JiveGlobals.getProperty(SUBJECT, "Message of the Day");
   }

   public void setMessage(String message) {
      JiveGlobals.setProperty(MESSAGE, message);
   }

   public String getMessage() {
      return JiveGlobals.getProperty(MESSAGE, "Big Brother is watching.");
   }

   public void setEnabled(boolean enable) {
      JiveGlobals.setProperty(ENABLED, Boolean.toString(enable));
   }

   public boolean isEnabled() {
      return JiveGlobals.getBooleanProperty(ENABLED, false);
   }

   private class MotDSessionEventListener implements SessionEventListener {
      public void sessionCreated(Session session) {         
         if (isEnabled()) {
            final Message message = new Message();
            message.setTo(session.getAddress());
            message.setFrom(serverAddress);
            message.setSubject(getSubject());
            message.setBody(getMessage());

            TimerTask messageTask = new TimerTask() {
               @Override
            public void run() {
                  router.route(message);
               }
            };

            TaskEngine.getInstance().schedule(messageTask, Duration.ofSeconds(5));
         }
      }

      public void sessionDestroyed(Session session) {
         //ignore
      }

      public void resourceBound(Session session) {
         // Do nothing.
      }

      public void anonymousSessionCreated(Session session) {
         //ignore
      }

      public void anonymousSessionDestroyed(Session session) {
         //ignore
      }
   }
}
