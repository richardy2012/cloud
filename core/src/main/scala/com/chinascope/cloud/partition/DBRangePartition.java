package com.chinascope.cloud.partition;

import java.util.Vector;

/**
 * Created by soledede.weng on 2016/6/15.
 */
public class DBRangePartition extends RangePartition {

    private String partitionField;


    public DBRangePartition() {
    }

    public DBRangePartition(String partitionField) {
        this.partitionField = partitionField;
    }

    public String getPartitionField() {
        return partitionField;
    }

    public void setPartitionField(String partitionField) {
        this.partitionField = partitionField;
    }


}
