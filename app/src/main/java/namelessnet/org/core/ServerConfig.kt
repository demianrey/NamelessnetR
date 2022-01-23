package namelessnet.org.core

import namelessnet.org.Constants.TAG_AGENT
import namelessnet.org.Constants.TAG_BLOCKED
import namelessnet.org.Constants.TAG_DIRECT
import namelessnet.org.xtreme.Utils

data class ServerConfig(
    val configVersion: Int = 3,
    val configType: protocols,
    var subscriptionId: String = "",
    val addedTime: Long = System.currentTimeMillis(),
    var remarks: String = "",
    val outboundBean: V2rayConfig.OutboundBean? = null,
    var fullConfig: V2rayConfig? = null
) {
    companion object {
        fun create(configType: protocols): ServerConfig {
            when (configType) {
                protocols.VMESS, protocols.VLESS ->
                    return ServerConfig(
                        configType = configType,
                        outboundBean = V2rayConfig.OutboundBean(
                            protocol = configType.name.lowercase(),
                            settings = V2rayConfig.OutboundBean.OutSettingsBean(
                                vnext = listOf(
                                    V2rayConfig.OutboundBean.OutSettingsBean.VnextBean(
                                        users = listOf(V2rayConfig.OutboundBean.OutSettingsBean.VnextBean.UsersBean())
                                    )
                                )
                            ),
                            streamSettings = V2rayConfig.OutboundBean.StreamSettingsBean()
                        )
                    )
                protocols.PRIVATE ->
                    return ServerConfig(configType = configType)
                protocols.SHADOWSOCKS, protocols.SOCKS, protocols.TROJAN ->
                    return ServerConfig(
                        configType = configType,
                        outboundBean = V2rayConfig.OutboundBean(
                            protocol = configType.name.lowercase(),
                            settings = V2rayConfig.OutboundBean.OutSettingsBean(
                                servers = listOf(V2rayConfig.OutboundBean.OutSettingsBean.ServersBean())
                            ),
                            streamSettings = V2rayConfig.OutboundBean.StreamSettingsBean()
                        )
                    )
            }
        }
    }

    fun getProxyOutbound(): V2rayConfig.OutboundBean? {
        if (configType != protocols.PRIVATE) {
            return outboundBean
        }
        return fullConfig?.getProxyOutbound()
    }

    fun getAllOutboundTags(): MutableList<String> {
        if (configType != protocols.PRIVATE) {
            return mutableListOf(TAG_AGENT, TAG_DIRECT, TAG_BLOCKED)
        }
        fullConfig?.let { config ->
            return config.outbounds.map { it.tag }.toMutableList()
        }
        return mutableListOf()
    }

    fun getV2rayPointDomainAndPort(): String {
        val address = getProxyOutbound()?.getServerAddress().orEmpty()
        val port = getProxyOutbound()?.getServerPort()
        return if (Utils.isIpv6Address(address)) {
            String.format("[%s]:%s", address, port)
        } else {
            String.format("%s:%s", address, port)
        }
    }
}
