package com.atlassian.theplugin.bamboo.configuration;

/**
 * Initial (dummy) implementation of bamboo config
 * User: sginter
 * Date: Jan 10, 2008
 * Time: 12:00:32 PM
 */
class BambooConfigurationImpl implements Configuration {
    ServerImpl server = new ServerImpl();

    public Server getServer() {
        return server;
    }

    public void setServer(Server newConfiguration) {
        server = new ServerImpl(newConfiguration);
    }

    /**
 * Configuration for a single Bamboo server.
     * User: sginter
     * Date: Jan 10, 2008
     * Time: 11:51:08 AM
     */
    static class ServerImpl implements Server {
        private String name;
        private String urlString;
        private String username;
        private String password;

        ServerImpl(Server s) {
            name = s.getName();
            urlString = s.getUrlString();
            username = s.getUsername();
            password = s.getPassword();
        }

        ServerImpl() {
        }

        public String getName() {
            return name;
        }

        void setName(String name) {
            this.name = name;
        }

        public String getUrlString() {
            return urlString;
        }

        void setUrlString(String urlString) {
            this.urlString = urlString;
        }

        public String getUsername() {
            return username;
        }

        void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        void setPassword(String password) {
            this.password = password;
        }
    }
}
