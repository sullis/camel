/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.file;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.apache.camel.ContextTestSupport;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.junit.jupiter.api.Test;

import static org.awaitility.Awaitility.await;

/**
 *
 */
public class NewFileConsumerTest extends ContextTestSupport {

    private MyFileEndpoint myFile;

    @Test
    public void testNewFileConsumer() throws Exception {
        getMockEndpoint("mock:result").expectedMessageCount(1);

        template.sendBodyAndHeader(fileUri(), "Hello World", Exchange.FILE_NAME, "hello.txt");

        assertMockEndpointsSatisfied();
        oneExchangeDone.matchesWaitTime();

        await("postPollCheck invocation").atMost(1, TimeUnit.SECONDS).until(myFile::isPost);
    }

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            @Override
            public void configure() {
                myFile = new MyFileEndpoint();
                myFile.setCamelContext(context);
                myFile.setFile(testDirectory().toFile());
                myFile.setDelay(10);
                myFile.setInitialDelay(0);

                from(myFile).to("mock:result");
            }
        };
    }

    private static class MyFileEndpoint extends FileEndpoint {

        private volatile boolean post;

        @Override
        protected FileConsumer newFileConsumer(Processor processor, GenericFileOperations<File> operations) {
            return new FileConsumer(this, processor, operations, createGenericFileStrategy()) {
                @Override
                protected void postPollCheck(int polledMessages) {
                    post = true;
                }
            };
        }

        public boolean isPost() {
            return post;
        }
    }

}
