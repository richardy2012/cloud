package com.csf.cloud.entity.juchao;

import java.util.Date;

/**
 * Created by soledede.weng on 2016/6/27.
 */
public class TbJuchaoTestS {
    private Integer id;
    private String name;
    private Date littleDate;

    public TbJuchaoTestS() {
    }

    public TbJuchaoTestS(Integer id, String name, Date littleDate) {
        this.id = id;
        this.name = name;
        this.littleDate = littleDate;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getLittleDate() {
        return littleDate;
    }

    public void setLittleDate(Date littleDate) {
        this.littleDate = littleDate;
    }

    @Override
    public String toString() {
        return "TbJuchaoTestS{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", littleDate=" + littleDate +
                '}';
    }
}
