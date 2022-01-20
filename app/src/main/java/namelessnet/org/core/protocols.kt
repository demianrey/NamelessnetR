package namelessnet.org.core

enum class protocols(val value: Int, val protocalScheme: String) {
    VMESS(1, "vmess://"),
    PRIVATE(2, ""),
    SHADOWSOCKS(3, "ss://"),
    SOCKS(4, "socks://"),
    VLESS(5, "vless://"),
    TROJAN(6, "trojan://"),
    PSIPHON(7, "psiphon://"),
    SSHDIRECT(8, "ssh://"),
    SLOWDNS(9, "dns://"),
    SSL(10, "ssl://");


    companion object {
        fun fromInt(value: Int) = values().firstOrNull { it.value == value }
    }
}