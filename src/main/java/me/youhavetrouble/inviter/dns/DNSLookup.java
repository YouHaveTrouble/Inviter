package me.youhavetrouble.inviter.dns;

import me.youhavetrouble.inviter.Main;
import org.jetbrains.annotations.NotNull;

import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

public class DNSLookup {

    public static Set<String> getTxtRecords(@NotNull String hostname) throws Exception {
        Hashtable<String, String> env = new Hashtable<>();
        env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
        env.put("java.naming.provider.url", "dns://1.1.1.1");
        env.put("com.sun.jndi.dns.cache.ttl", "0");
        Set<String> txtRecords = new HashSet<>();

        DirContext ctx = new InitialDirContext(env);
        Attributes attrs = ctx.getAttributes(hostname, new String[]{"TXT"});
        Attribute txtAttr = attrs.get("TXT");

        if (txtAttr != null) {
            for (int i = 0; i < txtAttr.size(); i++) {
                Object record = txtAttr.get(i);
                if (record instanceof String recordString) {
                    txtRecords.add(recordString);
                } else if (record instanceof byte[]) {
                    txtRecords.add(new String((byte[]) record));
                } else {
                    txtRecords.add(record.toString());
                }
            }
        }

        return txtRecords;
    }
}
