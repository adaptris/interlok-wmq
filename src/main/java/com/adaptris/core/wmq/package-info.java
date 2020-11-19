/**
 * Provides adapter connectivity to WebsphereMQ based on the native MQ API.
 * <p>
 * You will need to have installed and configured the WebsphereMQ Client software for your platform.
 * The jars from the WebsphereMQ Client software should be copied into the adapter's lib directory.
 * </p>
 * <p>
 * The default message options that are used are sensible for most requirements, and there should be
 * no reason to change those values. If changes are required, then access to the WebsphereMQ
 * Information center will help when trying to find out the correct integer values to configure.
 * Alternatively, decompiling the MQC.class in com.ibm.mq will give you the literal values
 * associated with each constant.
 * </p>
 *
 * @deprecated since 3.11.1 without replacement since IBM recommend you use JMS instead
 */
 package com.adaptris.core.wmq;