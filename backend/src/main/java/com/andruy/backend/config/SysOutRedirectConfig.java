package com.andruy.backend.config;

import java.io.PrintStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
public class SysOutRedirectConfig {
    @PostConstruct
    public void init() {
        System.setOut(new LoggingPrintStream(System.out, LoggerFactory.getLogger("STDOUT"), false));
        System.setErr(new LoggingPrintStream(System.err, LoggerFactory.getLogger("STDERR"), true));
    }

    private static class LoggingPrintStream extends PrintStream {
        private static final ThreadLocal<Boolean> logging = ThreadLocal.withInitial(() -> false);
        private final Logger logger;
        private final boolean isError;

        LoggingPrintStream(PrintStream original, Logger logger, boolean isError) {
            super(original, true);
            this.logger = logger;
            this.isError = isError;
        }

        @Override
        public void write(byte[] buf, int off, int len) {
            super.write(buf, off, len);
            if (!logging.get()) {
                logging.set(true);
                try {
                    String msg = new String(buf, off, len).stripTrailing();
                    if (!msg.isEmpty()) {
                        if (isError) {
                            logger.warn(msg);
                        } else {
                            logger.info(msg);
                        }
                    }
                } finally {
                    logging.set(false);
                }
            }
        }
    }
}
