package combinedshop

import org.springframework.boot.context.embedded.ServletContextInitializer
import org.springframework.context.annotation.Bean

class DefaultWsChatConfig {
    @Bean
    public ServletContextInitializer myInitializer() {
        return new ServletContextInitializer() {
            @Override
            public void onStartup(ServletContext servletContext) throws ServletException {
                servletContext.addListener(ChatEndpoint)
            }
        }
    }

}
