package org.ethereum.vmtrace;

import org.json.simple.JSONArray;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Roman Mandeleil
 * @since 28.10.2014
 */
public class ProgramTrace {

    byte[] txHash;
    List<Op> ops = new ArrayList<>();

    public void setTxHash(byte[] txHash) {
        this.txHash = txHash;
    }

    public void addOp(Op op) {
        ops.add(op);
    }

    /**
     * used for merging sub calls execution
     *
     * @param programTrace
     */
    public void merge(ProgramTrace programTrace) {

        this.ops.addAll(programTrace.ops);
    }

    public String getJsonString() {
        return JSONArray.toJSONString(ops);
    }
}
