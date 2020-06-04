/*******************************************************************************
 * Copyright (c) 2018-05-29 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 * Auto Generate By foreveross.com Quick Deliver Platform. 
 ******************************************************************************/
package com.iff.docker.modules.util;

import ch.qos.logback.core.OutputStreamAppender;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.util.Assert;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * LogKafkaHelper
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2018-05-29
 * auto generate by qdp.
 */
@Slf4j
public class LogKafkaHelper {
    private static final String valuesSeparator = "`@`";
    private static final String kvSeparator = "`#`";
    private static ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private static List<ProducerRecord<String, String>> logs = new ArrayList<ProducerRecord<String, String>>(1024);
    private static List<ProducerRecord<String, String>> logsCache = new ArrayList<ProducerRecord<String, String>>(1024);
    private static KafkaInstant kafkaLog;
    private static long lastSend = System.currentTimeMillis();
    private static ScheduledExecutorService scheduled = Executors.newScheduledThreadPool(5, new BasicThreadFactory.Builder().build());

    public static void main(String[] args) {
        Properties props = new Properties();
        String serializer = StringSerializer.class.getName();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "47.106.103.169:19093");
        //props.put(ProducerConfig.CLIENT_ID_CONFIG, "FOSS.LOG");
        //props.put(ProducerConfig.ACKS_CONFIG, "0");
        //props.put("max.block.ms", "30000");
        String jaasTemplate = "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"%s\" password=\"%s\";";
        String jaasCfg = String.format(jaasTemplate, "admin", "iffiff");
        props.put("security.protocol", "SASL_PLAINTEXT");
        props.put("sasl.mechanism", "PLAIN");
        props.put("sasl.jaas.config", jaasCfg);
        props.put("key.serializer", serializer);
        props.put("value.serializer", serializer);
        KafkaProducer<String, String> producer = new KafkaProducer<String, String>(props);
        for (int i = 0; i < 100; i++) {
            producer.send(new ProducerRecord<String, String>("test", JSON.toJSONString(new JSONObject().fluentPut("name", "tyler").fluentPut("date", new Date()))));
        }
        producer.flush();
        Thread t =new Thread(new Runnable() {
            public void run() {
                props.put("key.deserializer", StringDeserializer.class.getName());
                props.put("value.deserializer", StringDeserializer.class.getName());
                KafkaConsumer consumer = new KafkaConsumer(props);
                consumer.subscribe(Arrays.asList("test"));
                try {
                    while (true) {
                        ConsumerRecords<String, String> records = consumer.poll(1000);
                        for (ConsumerRecord<String, String> record : records) {
                            System.out.printf("offset = %d, key = %s, value = %s%n", record.offset(), record.key(), record.value());
                        }
                    }
                } finally {
                    consumer.close();
                }
            }
        });
        t.start();
        try {
        t.join();
            TimeUnit.SECONDS.sleep(10);
            IOUtils.closeQuietly(producer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main2(String[] args) {
        Properties props = new Properties();
        String serializer = StringSerializer.class.getName();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "47.106.103.169:9092");
        //props.put(ProducerConfig.CLIENT_ID_CONFIG, "FOSS.LOG");
        //props.put(ProducerConfig.ACKS_CONFIG, "0");
        //props.put("max.block.ms", "30000");
        props.put("key.serializer", serializer);
        props.put("value.serializer", serializer);
        KafkaProducer<String, String> producer = new KafkaProducer<String, String>(props);
        producer.send(new ProducerRecord<String, String>("applogs", JSON.toJSONString(new JSONObject().fluentPut("name", "tyler").fluentPut("date", new Date()))));
        try {
            TimeUnit.SECONDS.sleep(10);
            IOUtils.closeQuietly(producer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main1(String[] args) {
//        init("47.106.103.169:9092", "applogs");
//        start();
//        log(new JSONObject().fluentPut("name", "tyler").fluentPut("date", new Date()));
//        log(new JSONObject().fluentPut("name", "tyler").fluentPut("date", new Date()));
//        log(new JSONObject().fluentPut("name", "tyler").fluentPut("date", new Date()));
//        close();
//        try {
//            TimeUnit.SECONDS.sleep(10);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        System.exit(2);
    }

    public static void init(Properties props) {
        kafkaLog = KafkaInstant.config(props);
    }

    public static void start() {
        Assert.notNull(kafkaLog, "LOG.KAFKA kafka is not init, please invoke LogKafkaHelper.init to init kafka first.");
        kafkaLog.init();
        scheduled.scheduleWithFixedDelay(new Runnable() {
            public void run() {
                send();
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    public static void send() {
        try {
            lock.writeLock().lock();
            List<ProducerRecord<String, String>> tmp = logs;
            logs = logsCache;
            logsCache = tmp;
            if (logsCache.size() < 1) {
                return;
            }
        } finally {
            lock.writeLock().unlock();
        }
        final ProducerRecord<String, String>[][] prss = new ProducerRecord[1][];
        try {
            lock.writeLock().lock();
            prss[0] = logsCache.toArray(new ProducerRecord[logsCache.size()]);
            logsCache.clear();
            if (prss[0].length < 1) {
                return;
            }
        } finally {
            lock.writeLock().unlock();
        }
        if (kafkaLog.getProducer() == null) {
            log.warn("LOG.KAFKA kafka not start yet, please invoke LogKafkaHelper.start to start Kafka first!");
            return;
        }
        try {
            scheduled.execute(new Runnable() {
                public void run() {
                    ProducerRecord<String, String>[] prs = prss[0];
                    for (int i = 0; i < prs.length; i++) {
                        final ProducerRecord<String, String> pr = prs[i];
                        kafkaLog.getProducer().send(pr, new Callback() {
                            public void onCompletion(RecordMetadata metadata, Exception exception) {
                                if (exception != null) {
                                    try {
                                        log.warn("Re-add message: " + pr.key());
                                        logs.add(pr);
                                        kafkaLog.renew();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void log(String message) {
        //Assert.notNull(kafkaLog, "LOG.KAFKA kafka is not init, please invoke LogKafkaHelper.init to init kafka first.");
        if (kafkaLog != null && !kafkaLog.isEnable()) {
            return;
        }
        if (StringUtils.isNotBlank(message)) {
            if (kafkaLog != null) {
                saveLog(new ProducerRecord<String, String>(kafkaLog.getTopic(), new JSONObject().fluentPut("message", message).fluentPut("date", new Date()).toJSONString()));
            }
        }
    }

    public static void logJson(String json) {
        //Assert.notNull(kafkaLog, "LOG.KAFKA kafka is not init, please invoke LogKafkaHelper.init to init kafka first.");
        if (kafkaLog != null && !kafkaLog.isEnable()) {
            return;
        }
        if (StringUtils.isNotBlank(json)) {
            if (kafkaLog != null) {
                saveLog(new ProducerRecord<String, String>(kafkaLog.getTopic(), json));
            }
        }
    }

    public static void log(final Map<?, ?> data) {
        //Assert.notNull(kafkaLog, "LOG.KAFKA kafka is not init, please invoke LogKafkaHelper.init to init kafka first.");
        if (kafkaLog != null && !kafkaLog.isEnable()) {
            return;
        }
        if (data != null && data.size() > 0) {
            if (kafkaLog != null) {
                saveLog(new ProducerRecord<String, String>(kafkaLog.getTopic(), JSON.toJSONString(data)));
            }
        }
    }

    public static void close() {
        Assert.notNull(kafkaLog, "LOG.KAFKA kafka is not init, please invoke LogKafkaHelper.init to init kafka first.");
        send();
        scheduled.execute(new Runnable() {
            public void run() {
                kafkaLog.getProducer().flush();
                try {
                    TimeUnit.SECONDS.sleep(10);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                IOUtils.closeQuietly(kafkaLog.getProducer());
            }
        });
    }

    private static void saveLog(ProducerRecord pr) {
        if (kafkaLog == null) {
            log.warn("LOG.KAFKA kafka not init yet, please invoke LogKafkaHelper.init to init Kafka first!");
        } else if (kafkaLog.getProducer() == null) {
            log.warn("LOG.KAFKA kafka not start yet, please invoke LogKafkaHelper.start to start Kafka first!");
        }
        try {
            lock.readLock().lock();
            logs.add(pr);
        } finally {
            lock.readLock().unlock();
        }
        if (logs.size() < 1000) {
            return;
        }
        if (logsCache.size() > 0) {
            log.warn("LOG.KAFKA kafka too many unsaved logs.");
            if (logs.size() > 2000) {
                try {
                    lock.writeLock().lock();
                    log.warn("LOG.KAFKA UNSAVED.LOGS [NOKAFKA]: " + JSON.toJSONString(logsCache));
                    logsCache.clear();
                } finally {
                    lock.writeLock().unlock();
                }
            }
        }
        send();
    }

    public static class KafkaInstant implements Closeable {
        private String id;
        private String topic;
        private Properties props;
        private Producer<String, String> producer;
        private long lastRenew = System.currentTimeMillis();
        private boolean enable = false;

        public static KafkaInstant config(Properties props) {
            Assert.notNull(props, "LOG.KAFKA kafka props is required.");
            KafkaInstant ins = new KafkaInstant();
            {
                ins.enable = "true".equals((String) props.remove("enable"));
                if (!ins.enable) {
                    log.info("LOG.KAFKA is NOT enabled.");
                    return ins;
                }
                log.info("LOG.KAFKA is enabled.");
            }
            String topic = (String) props.remove("topic");
            String brokers = props.getProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG);
            Assert.hasText(topic, "LOG.KAFKA kafka topic is required.");
            Assert.hasText(brokers, "LOG.KAFKA kafka broker is required.");
            ins.topic = topic;
            ins.id = brokers + ":" + topic;
            ins.props = props;
            if (StringUtils.isBlank(props.getProperty("key.serializer"))) {
                props.put("key.serializer", StringSerializer.class.getName());
            }
            if (StringUtils.isBlank(props.getProperty("value.serializer"))) {
                props.put("value.serializer", StringSerializer.class.getName());
            }
            //props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
            //props.put("group.id", StringUtils.defaultString(username, "FOSS.LOG") + "-consumer");
            //props.put(ProducerConfig.CLIENT_ID_CONFIG, "FOSS.LOG");
            //props.put(ProducerConfig.ACKS_CONFIG, "0");
            //props.put("metadata.fetch.timeout.ms", "1000");
            //props.put("max.block.ms", "30000");
            //props.put("message.timeout.ms", "1000");
            //props.put("message.send.max.retries", "0");
            //props.put("enable.auto.commit", "true");
            //props.put("auto.commit.interval.ms", "1000");
            //props.put("auto.offset.reset", "earliest");
            //props.put("session.timeout.ms", "30000");
            //props.put("key.deserializer", deserializer);
            //props.put("value.deserializer", deserializer);
            //            if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
            //                String jaasTemplate = "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"%s\" password=\"%s\";";
            //                String jaasCfg = String.format(jaasTemplate, username, password);
            //                props.put("security.protocol", "SASL_SSL");
            //                props.put("sasl.mechanism", "SCRAM-SHA-256");
            //                props.put("sasl.jaas.config", jaasCfg);
            //            }
            return ins;
        }

        public void init() {
            if (!enable) {
                return;
            }
            Assert.notNull(props, "LOG.KAFKA please invoke KafkaInstant.config to config first!");
            if (producer != null) {
                return;
            }
            producer = new KafkaProducer<String, String>(props);
        }

        public KafkaInstant renew() {
            if (System.currentTimeMillis() - lastRenew < 3 * 1000) {
                return this;
            }
            try {
                producer.close(5, TimeUnit.SECONDS);
            } catch (Exception e) {
                e.printStackTrace();
            }
            producer = new KafkaProducer<String, String>(props);
            lastRenew = System.currentTimeMillis();
            return this;
        }

        public void close() {
            IOUtils.closeQuietly(producer);
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getTopic() {
            return topic;
        }

        public void setTopic(String topic) {
            this.topic = topic;
        }

        public Properties getProps() {
            return props;
        }

        public void setProps(Properties props) {
            this.props = props;
        }

        public Producer<String, String> getProducer() {
            return producer;
        }

        public void setProducer(Producer<String, String> producer) {
            this.producer = producer;
        }

        public boolean isEnable() {
            return enable;
        }
    }

    public static class KafkaAppender extends OutputStreamAppender {
        private ByteArrayOutputStream baos = null;

        public void start() {
            if (baos == null) {
                baos = new ByteArrayOutputStream(2048);
            }
            setOutputStream(baos);
            super.start();
        }

        public void doAppend(Object eventObject) {
            if (kafkaLog != null && !kafkaLog.isEnable()) {
                return;
            }
            super.doAppend(eventObject);
        }

        protected void subAppend(Object event) {
            super.subAppend(event);
            try {
                JSONObject data = new JSONObject();
                String content = baos.toString("UTF-8");
                baos.reset();
                if (!StringUtils.startsWith(content, "[KAFKALOG] ")) {
                    return;
                }
                content = content.substring("[KAFKALOG] ".length());
                String[] kvs = StringUtils.splitByWholeSeparator(content, valuesSeparator);
                if (kvs.length == 0) {
                    return;
                } else if (kvs.length == 1) {
                    data.put("message", kvs[0]);
                } else {
                    for (String kv : kvs) {
                        String[] split = StringUtils.splitByWholeSeparator(kv, kvSeparator);
                        if (split.length != 2 || StringUtils.isBlank(split[0])) {
                            continue;
                        }
                        String key = split[0], value = split[1];
                        data.put(key, value);
                    }
                }
                if (data.size() > 0) {
                    log(data);
                }
            } catch (Exception e) {
                log.error("KafkaAppender error:", e);
            }
        }
    }
}
