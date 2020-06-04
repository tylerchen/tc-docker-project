/*******************************************************************************
 * Copyright (c) 2020-06-01 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package com.iff.docker.modules.app.vo.form;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Arrays;
import java.util.Map;

/**
 * ContainerFormVO
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2020-06-01
 */
@ApiModel
@Data
public class ContainerFormVO {
    public static String[] CAP_DEFAULT = new String[]{"AUDIT_WRITE", "SETFCAP", "MKNOD", "CHOWN", "NET_RAW", "DAC_OVERRIDE", "FOWNER", "FSETID", "KILL", "SETGID", "SETUID", "NET_BIND_SERVICE", "SYS_CHROOT", "SETPCAP"};
    public static String[] CAP_ALL = new String[]{"AUDIT_WRITE", "SETFCAP", "MKNOD", "CHOWN", "NET_RAW", "DAC_OVERRIDE", "FOWNER", "FSETID", "KILL", "SETGID", "SETUID", "NET_BIND_SERVICE", "SYS_CHROOT", "SETPCAP", "AUDIT_CONTROL", "BLOCK_SUSPEND", "DAC_READ_SEARCH", "IPC_LOCK", "IPC_OWNER", "LEASE", "LINUX_IMMUTABLE", "MAC_ADMIN", "MAC_OVERRIDE", "NET_ADMIN", "NET_BROADCAST", "SYS_ADMIN", "SYS_BOOT", "SYSLOG", "SYS_MODULE", "SYS_NICE", "SYS_PACCT", "SYS_PTRACE", "SYS_RAWIO", "SYS_RESOURCE", "SYS_TIME", "SYS_TTY_CONFIG", "WAKE_ALARM"};
    @ApiModelProperty(required = true)
    @JsonProperty("Image")
    private String image;
    @ApiModelProperty(required = true)
    @JsonProperty("Name")
    private String name;
    @ApiModelProperty(notes = "A PortBinding corresponds to the --publish (-p) option of the docker run (and * similar) CLI command for adding port bindings to a container")
    @JsonProperty("PortBindings")
    private String[] portBindings;
    @JsonProperty("Cmd")
    private String[] cmd;
    @JsonProperty("Entrypoint")
    private String[] entrypoint;
    @JsonProperty("WorkingDir")
    private String workingDir;
    @JsonProperty("User")
    private String user;
    @ApiModelProperty(allowableValues = "NONE,DEFAULT,LOCAL,ETWLOGS,JSON_FILE,SYSLOG,JOURNALD,GELF,FLUENTD,AWSLOGS,DB,SPLUNK,GCPLOGS")
    @JsonProperty("LoggingType")
    private String loggingType;
    @JsonProperty("LoggingConfig")
    private Map<String, String> loggingConfig;
    @JsonProperty("VolumesBinds")
    private String[] volumeBinds;
    @ApiModelProperty(example = "bridge", notes = "Supported standard values are: bridge, host, none, and container:<name|id>")
    @JsonProperty("NetworkMode")
    private String networkMode;
    @JsonProperty("Hostname")
    private String hostname;
    @JsonProperty("Domainname")
    private String domainname;
    @JsonProperty("MacAddress")
    private String macAddress;
    @JsonProperty("Ipv4Address")
    private String ipv4Address;
    @JsonProperty("Ipv6Address")
    private String ipv6Address;
    @JsonProperty("Dns")
    private String[] dns;
    @JsonProperty("DnsOptions")
    private String[] dnsOptions;
    @JsonProperty("DnsSearch")
    private String[] dnsSearch;
    @ApiModelProperty(notes = "A list of hostnames/IP mappings to add to the container's /etc/hosts file. Specified in the form [\"hostname:IP\"].")
    @JsonProperty("ExtraHosts")
    private String[] extraHosts;
    @ApiModelProperty(notes = "A list of environment variables to set inside the container in the form [\"VAR=value\", ...]")
    @JsonProperty("Env")
    private String[] env;
    @JsonProperty("Labels")
    private Map<String, String> labels;
    @ApiModelProperty(notes = "empty value, always, unless-stopped,on-failure")
    @JsonProperty("RestartPolicy")
    private String restartPolicy;
    @JsonProperty("RestartPolicyFailureRetry")
    private int restartPolicyFailureRetry;
    @ApiModelProperty(example = "false")
    @JsonProperty("Privileged")
    private boolean privileged = false;
    @ApiModelProperty(notes = "empty value or runc")
    @JsonProperty("Runtime")
    private String runtime;
    @JsonProperty("Device")
    private String[] device;
    @ApiModelProperty(notes = "memory unit: B(Byte),K(KB),M(MB),G(GB)")
    @JsonProperty("Memory")
    private String memory;
    @ApiModelProperty(notes = "memory unit: B(Byte),K(KB),M(MB),G(GB)")
    @JsonProperty("MemoryReservation")
    private String memoryReservation;
    /*NanoCpus: cpuPercent * pow(10,9)*/
    @JsonProperty("CpuPercent")
    private Double cpuPercent;
    @ApiModelProperty(notes = "The test to perform. Possible values are:\n" +
            "[] inherit healthcheck from image or parent image\n" +
            "[\"NONE\"] disable healthcheck\n" +
            "[\"CMD\", args...] exec arguments directly\n" +
            "[\"CMD-SHELL\", command] run command with system's default shell")
    @JsonProperty("HealthcheckTest")
    private String[] healthcheckTest;
    @ApiModelProperty(example = "0", notes = "The time to wait between checks in nanoseconds. It should be 0 or at least 1000000 (1 ms). 0 means inherit.")
    @JsonProperty("HealthcheckInterval")
    private long healthcheckInterval = 0;
    @ApiModelProperty(example = "0", notes = "The time to wait before considering the check to have hung. It should be 0 or at least 1000000 (1 ms). 0 means inherit.")
    @JsonProperty("HealthcheckTimeout")
    private long healthcheckTimeout = 0;
    @ApiModelProperty(example = "0", notes = "The number of consecutive failures needed to consider a container as unhealthy. 0 means inherit.")
    @JsonProperty("HealthcheckRetries")
    private int healthcheckRetries = 0;
    @ApiModelProperty(example = "0", notes = "Start period for the container to initialize before starting health-retries countdown in nanoseconds. It should be 0 or at least 1000000 (1 ms). 0 means inherit.")
    @JsonProperty("HealthcheckStartPeriod")
    private long healthcheckStartPeriod = 0;
    @JsonProperty("Shell")
    private String[] shell;
    @ApiModelProperty(example = "false")
    @JsonProperty("AutoRemove")
    private boolean autoRemove = false;
    @ApiModelProperty(allowableValues = "AUDIT_WRITE,SETFCAP,MKNOD,CHOWN,NET_RAW,DAC_OVERRIDE,FOWNER,FSETID,KILL,SETGID,SETUID,NET_BIND_SERVICE,SYS_CHROOT,SETPCAP,AUDIT_CONTROL,BLOCK_SUSPEND,DAC_READ_SEARCH,IPC_LOCK,IPC_OWNER,LEASE,LINUX_IMMUTABLE,MAC_ADMIN,MAC_OVERRIDE,NET_ADMIN,NET_BROADCAST,SYS_ADMIN,SYS_BOOT,SYSLOG,SYS_MODULE,SYS_NICE,SYS_PACCT,SYS_PTRACE,SYS_RAWIO,SYS_RESOURCE,SYS_TIME,SYS_TTY_CONFIG,WAKE_ALARM",
            example = "[\"AUDIT_WRITE\", \"SETFCAP\", \"MKNOD\", \"CHOWN\", \"NET_RAW\", \"DAC_OVERRIDE\", \"FOWNER\", \"FSETID\", \"KILL\", \"SETGID\", \"SETUID\", \"NET_BIND_SERVICE\", \"SYS_CHROOT\", \"SETPCAP\"]")
    @JsonProperty("Capabilities")
    private String[] capabilities = Arrays.copyOf(CAP_DEFAULT, CAP_DEFAULT.length, String[].class);
}
