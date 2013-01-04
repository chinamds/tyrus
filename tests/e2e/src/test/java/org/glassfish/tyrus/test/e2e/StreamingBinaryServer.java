/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package org.glassfish.tyrus.test.e2e;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.websocket.MessageHandler;
import javax.websocket.Session;
import javax.websocket.WebSocketOpen;
import javax.websocket.server.DefaultServerConfiguration;
import javax.websocket.server.WebSocketEndpoint;

/**
 * @author Danny Coward (danny.coward at oracle.com)
 * @author Martin Matula (martin.matula at oracle.com)
 */
@WebSocketEndpoint(value = "/streamingbinary",configuration = DefaultServerConfiguration.class)
public class StreamingBinaryServer {
    private Session session;
    static CountDownLatch messageLatch;
    private List<String> messages = new ArrayList<String>();

    @WebSocketOpen
    public void onOpen(Session session) {
        System.out.println("STREAMINGBSERVER opened !");
        this.session = session;

        session.addMessageHandler(new MessageHandler.Async<ByteBuffer>() {
            StringBuilder sb = new StringBuilder();

            @Override
            public void onMessage(ByteBuffer bb, boolean last) {
                System.out.println("STREAMINGBSERVER piece came: " + new String(bb.array()));
                sb.append( new String(bb.array()) );
                messages.add(new String(bb.array()));
                if (last) {
                    System.out.println("STREAMINGBSERVER whole message: " + sb.toString());
                    sb = new StringBuilder();
                    messageLatch.countDown();
                    reply();
                }
            }
        });



    }

    public void reply() {
        try {
            sendPartial(ByteBuffer.wrap(messages.get(0).getBytes()), false);
            sendPartial(ByteBuffer.wrap(messages.get(1).getBytes()), false);
            sendPartial(ByteBuffer.wrap(messages.get(2).getBytes()), false);
            sendPartial(ByteBuffer.wrap(messages.get(3).getBytes()), true);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendPartial(ByteBuffer bb, boolean isLast) throws IOException, InterruptedException {
        System.out.println("STREAMINGBSERVER Server sending: " + new String(bb.array()));
        session.getRemote().sendPartialBytes(bb, isLast);
    }
}

