package io.kettle.ctl;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class KettleProcessedArgs {

    public final List<String> args;

    public final Map<String, String> flags;

    public KettleProcessedArgs(List<String> args, Map<String, String> flags) {
        this.args = Collections.unmodifiableList(args);
        this.flags = Collections.unmodifiableMap(flags);
    }

    public String arg(int index) {
        return args.get(index);
    }

    public String flag(String flag) {
        return flags.get(flag);
    }

}
