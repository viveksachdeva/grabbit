/*
 * Copyright 2015 Time Warner Cable, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twcable.grabbit.server.batch.steps.jcrnodes

import com.twcable.grabbit.DateUtil
import com.twcable.grabbit.jcr.JcrNodeDecorator
import com.twcable.grabbit.proto.NodeProtos.Node as ProtoNode
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.batch.item.ItemProcessor

import javax.annotation.Nullable
import javax.jcr.Node as JcrNode

/**
 * This ItemProcessor takes javax.jcr.Node references from JcrNodesReader, and converts them into ProtoNode objects
 */
@Slf4j
@CompileStatic
class JcrNodesProcessor implements ItemProcessor<JcrNode, ProtoNode> {

    private String contentAfterDate

    void setContentAfterDate(String contentAfterDate) {
        this.contentAfterDate = contentAfterDate
    }

    /**
     * Converts a JCR Node to a {@link ProtoNode} object.
     * Returns null if current Node's processing needs to be ignored like for "rep:policy" nodes
     */
    @Override
    @Nullable
    ProtoNode process(JcrNode jcrNode) throws Exception {

        JcrNodeDecorator decoratedNode = new JcrNodeDecorator(jcrNode);

        //TODO: Access Control Lists nodes are not supported right now.
        if (decoratedNode.path.contains("rep:policy")) {
            log.info "Ignoring current node ${decoratedNode.innerNode}"
            return null
        }

        if (contentAfterDate) {
            final Date afterDate = DateUtil.getDateFromISOString(contentAfterDate)
            log.debug "ContentAfterDate received : ${afterDate}. Will ignore content created or modified before the afterDate"
            final date = decoratedNode.getModifiedOrCreatedDate()
            if (date && date.before(afterDate)) { //if there are no date properties, we treat nodes as new
                log.debug "Not sending any data older than ${afterDate}"
                return null
            }
        }

        // Skip this node because it has already been processed by its parent
        if(decoratedNode.isRequiredNode()) {
            return null;
        } else {
            // Build parent node
            return decoratedNode.toProtoNode();
        }
    }

}
