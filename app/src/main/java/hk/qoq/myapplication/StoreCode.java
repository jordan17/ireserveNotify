package hk.qoq.myapplication;

/**
 * Created by jordan17 on 2016/9/20.
 */
public enum StoreCode {
    R409("Causeway Bay"),
    R499("Canton Road"),
    R485("Festival Walk"),
    R428("ifc mall"),
    R610("New Town Plaza");
    String desc;
    String getDesc() {return this.desc;}
    StoreCode(String desc) {this.desc = desc;}
}
