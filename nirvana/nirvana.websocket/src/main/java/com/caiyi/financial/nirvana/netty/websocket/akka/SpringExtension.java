package com.caiyi.financial.nirvana.netty.websocket.akka;

import akka.actor.AbstractExtensionId;
import akka.actor.ExtendedActorSystem;
import akka.actor.Extension;
import akka.actor.Props;
import com.caiyi.financial.nirvana.netty.websocket.service.WebSocketServerHandler;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.context.ApplicationContext;

import java.util.Map;

/**
 * Created by been on 16/9/21.
 */

public class SpringExtension extends
        AbstractExtensionId<SpringExtension.SpringExt> {

    /**
     * The identifier used to access the SpringExtension.
     */
    public static SpringExtension SpringExtProvider = new SpringExtension();

    /**
     * Is used by Akka to instantiate the Extension identified by this
     * ExtensionId, internal use only.
     */
    @Override
    public SpringExt createExtension(ExtendedActorSystem system) {
        return new SpringExt();
    }

    /**
     * The Extension implementation.
     */
    public static class SpringExt implements Extension {
        private volatile ApplicationContext applicationContext;

        /**
         * Used to initialize the Spring application context for the extension.
         *
         * @param applicationContext
         */
        public void initialize(ApplicationContext applicationContext) {
            this.applicationContext = applicationContext;
        }

        /**
         * Create a Props for the specified actorBeanName using the
         * SpringActorProducer class.
         * <p>
         * <p>
         * added by been
         * 创建 websocket actor
         *
         * @param actorBeanName The name of the actor bean to create Props for
         * @return a Props that will create the named actor bean using Spring
         */
        public Props props(String actorBeanName, ChannelHandlerContext ctx,
                           WebSocketServerHandler handler,
                           Map<String, String[]> requestParameterMap,
                           Map<String, String> requestHeaderMap,
                           String url) {
            return Props.create(SpringActorProducer.class,
                    applicationContext, actorBeanName, ctx, handler, requestParameterMap, requestHeaderMap, url);
        }
    }
}
