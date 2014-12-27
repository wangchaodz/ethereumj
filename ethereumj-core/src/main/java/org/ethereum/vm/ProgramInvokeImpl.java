package org.ethereum.vm;

import org.ethereum.facade.Repository;

import java.math.BigInteger;

import java.util.Arrays;
import java.util.Map;

/**
 * @author Roman Mandeleil
 * @since 03.06.2014
 */
public class ProgramInvokeImpl implements ProgramInvoke {

    /**
     * TRANSACTION  env **
     */
    private DataWord address;
    private DataWord origin, caller,
            balance, gas, gasPrice, callValue;

    byte[] msgData;

    /**
     * BLOCK  env **
     */
    private DataWord prevHash, coinbase, timestamp,
            number, difficulty, gaslimit;

    Map<DataWord, DataWord> storage;

    private Repository repository;
    private boolean byTransaction = true;
    private boolean byTestingSuite = false;
    private int callDeep = 0;

    public ProgramInvokeImpl(DataWord address, DataWord origin, DataWord caller, DataWord balance,
                             DataWord gasPrice, DataWord gas, DataWord callValue, byte[] msgData,
                             DataWord lastHash, DataWord coinbase, DataWord timestamp, DataWord number, DataWord
                                     difficulty,
                             DataWord gaslimit, Repository repository, int callDeep) {

        // Transaction env
        this.address = address;
        this.origin = origin;
        this.caller = caller;
        this.balance = balance;
        this.gasPrice = gasPrice;
        this.gas = gas;
        this.callValue = callValue;
        this.msgData = msgData;

        // last Block env
        this.prevHash = lastHash;
        this.coinbase = coinbase;
        this.timestamp = timestamp;
        this.number = number;
        this.difficulty = difficulty;
        this.gaslimit = gaslimit;

        this.repository = repository;
        this.byTransaction = false;
        this.callDeep = callDeep;
    }

    public ProgramInvokeImpl(byte[] address, byte[] origin, byte[] caller, byte[] balance,
                             byte[] gasPrice, byte[] gas, byte[] callValue, byte[] msgData,
                             byte[] lastHash, byte[] coinbase, long timestamp, long number, byte[] difficulty,
                             long gaslimit,
                             Repository repository, boolean byTestingSuite) {
        this(address, origin, caller, balance, gasPrice, gas, callValue, msgData, lastHash, coinbase,
                timestamp, number, difficulty, gaslimit, repository);
        this.byTestingSuite = byTestingSuite;
    }


    public ProgramInvokeImpl(byte[] address, byte[] origin, byte[] caller, byte[] balance,
                             byte[] gasPrice, byte[] gas, byte[] callValue, byte[] msgData,
                             byte[] lastHash, byte[] coinbase, long timestamp, long number, byte[] difficulty,
                             long gaslimit,
                             Repository repository) {

        // Transaction env
        this.address = new DataWord(address);
        this.origin = new DataWord(origin);
        this.caller = new DataWord(caller);
        this.balance = new DataWord(balance);
        this.gasPrice = new DataWord(gasPrice);
        this.gas = new DataWord(gas);
        this.callValue = new DataWord(callValue);
        this.msgData = msgData;

        // last Block env
        this.prevHash = new DataWord(lastHash);
        this.coinbase = new DataWord(coinbase);
        this.timestamp = new DataWord(timestamp);
        this.number = new DataWord(number);
        this.difficulty = new DataWord(difficulty);
        this.gaslimit = new DataWord(gaslimit);

        this.repository = repository;
    }

    /*           ADDRESS op         */
    public DataWord getOwnerAddress() {
        return address;
    }

    /*           BALANCE op         */
    public DataWord getBalance() {
        return balance;
    }

    /*           ORIGIN op         */
    public DataWord getOriginAddress() {
        return origin;
    }

    /*           CALLER op         */
    public DataWord getCallerAddress() {
        return caller;
    }

    /*           GASPRICE op       */
    public DataWord getMinGasPrice() {
        return gasPrice;
    }

    /*           GAS op       */
    public DataWord getGas() {
        return gas;
    }

    /*          CALLVALUE op    */
    public DataWord getCallValue() {
        return callValue;
    }

      /*****************/
     /***  msg data ***/
    /*****************/
    /* NOTE: In the protocol there is no restriction on the maximum message data,
     * However msgData here is a byte[] and this can't hold more than 2^32-1
     */
    private static BigInteger MAX_MSG_DATA = BigInteger.valueOf(Integer.MAX_VALUE);

    /*     CALLDATALOAD  op   */
    public DataWord getDataValue(DataWord indexData) {

        BigInteger tempIndex = indexData.value();
        int index = tempIndex.intValue(); // possible overflow is caught below
        int size = 32; // maximum datavalue size

        if (msgData == null || index >= msgData.length
                || tempIndex.compareTo(MAX_MSG_DATA) == 1)
            return new DataWord();
        if (index + size > msgData.length)
            size = msgData.length - index;

        byte[] data = new byte[32];
        System.arraycopy(msgData, index, data, 0, size);
        return new DataWord(data);
    }

    /*  CALLDATASIZE */
    public DataWord getDataSize() {

        if (msgData == null || msgData.length == 0) return DataWord.ZERO;
        int size = msgData.length;
        return new DataWord(size);
    }

    /*  CALLDATACOPY */
    public byte[] getDataCopy(DataWord offsetData, DataWord lengthData) {

        int offset = offsetData.intValue();
        int length = lengthData.intValue();

        byte[] data = new byte[length];

        if (msgData == null) return data;
        if (offset > msgData.length) return data;
        if (offset + length > msgData.length) length = msgData.length - offset;

        System.arraycopy(msgData, offset, data, 0, length);

        return data;
    }


    @Override
    public int countNonZeroData() {

        int counter = 0;
        for (int i = 0; i < msgData.length; ++i) {

            if (msgData[i] != 0) ++counter;
        }
        return counter;
    }

    /*     PREVHASH op    */
    public DataWord getPrevHash() {
        return prevHash;
    }

    /*     COINBASE op    */
    public DataWord getCoinbase() {
        return coinbase;
    }

    /*     TIMESTAMP op    */
    public DataWord getTimestamp() {
        return timestamp;
    }

    /*     NUMBER op    */
    public DataWord getNumber() {
        return number;
    }

    /*     DIFFICULTY op    */
    public DataWord getDifficulty() {
        return difficulty;
    }

    /*     GASLIMIT op    */
    public DataWord getGaslimit() {
        return gaslimit;
    }

    /*  Storage */
    public Map<DataWord, DataWord> getStorage() {
        return storage;
    }

    public Repository getRepository() {
        return repository;
    }

    @Override
    public boolean byTransaction() {
        return byTransaction;
    }

    @Override
    public boolean byTestingSuite() {
        return byTestingSuite;
    }

    @Override
    public int getCallDeep() {
        return this.callDeep;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProgramInvokeImpl that = (ProgramInvokeImpl) o;

        if (byTestingSuite != that.byTestingSuite) return false;
        if (byTransaction != that.byTransaction) return false;
        if (address != null ? !address.equals(that.address) : that.address != null) return false;
        if (balance != null ? !balance.equals(that.balance) : that.balance != null) return false;
        if (callValue != null ? !callValue.equals(that.callValue) : that.callValue != null) return false;
        if (caller != null ? !caller.equals(that.caller) : that.caller != null) return false;
        if (coinbase != null ? !coinbase.equals(that.coinbase) : that.coinbase != null) return false;
        if (difficulty != null ? !difficulty.equals(that.difficulty) : that.difficulty != null) return false;
        if (gas != null ? !gas.equals(that.gas) : that.gas != null) return false;
        if (gasPrice != null ? !gasPrice.equals(that.gasPrice) : that.gasPrice != null) return false;
        if (gaslimit != null ? !gaslimit.equals(that.gaslimit) : that.gaslimit != null) return false;
        if (!Arrays.equals(msgData, that.msgData)) return false;
        if (number != null ? !number.equals(that.number) : that.number != null) return false;
        if (origin != null ? !origin.equals(that.origin) : that.origin != null) return false;
        if (prevHash != null ? !prevHash.equals(that.prevHash) : that.prevHash != null) return false;
        if (repository != null ? !repository.equals(that.repository) : that.repository != null) return false;
        if (storage != null ? !storage.equals(that.storage) : that.storage != null) return false;
        if (timestamp != null ? !timestamp.equals(that.timestamp) : that.timestamp != null) return false;

        return true;
    }

    @Override
    public String toString() {
        return "ProgramInvokeImpl{" +
                "address=" + address +
                ", origin=" + origin +
                ", caller=" + caller +
                ", balance=" + balance +
                ", gas=" + gas +
                ", gasPrice=" + gasPrice +
                ", callValue=" + callValue +
                ", msgData=" + Arrays.toString(msgData) +
                ", prevHash=" + prevHash +
                ", coinbase=" + coinbase +
                ", timestamp=" + timestamp +
                ", number=" + number +
                ", difficulty=" + difficulty +
                ", gaslimit=" + gaslimit +
                ", storage=" + storage +
                ", repository=" + repository +
                ", byTransaction=" + byTransaction +
                ", byTestingSuite=" + byTestingSuite +
                ", callDeep=" + callDeep +
                '}';
    }
}
