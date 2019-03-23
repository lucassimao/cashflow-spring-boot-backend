package com.lucassimao.fluxodecaixa.model;

import static com.lucassimao.fluxodecaixa.model.TenantEntity.TENANT_FILTER_ARGUMENT_NAME;
import static com.lucassimao.fluxodecaixa.model.TenantEntity.TENANT_FILTER_NAME;

import javax.persistence.Column;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

@MappedSuperclass()
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@FilterDef(
  name = TENANT_FILTER_NAME, 
  parameters = @ParamDef(name = TENANT_FILTER_ARGUMENT_NAME, type = "long"), 
  defaultCondition =  " tenant_id = :" + TENANT_FILTER_ARGUMENT_NAME)
@Filter(name = TENANT_FILTER_NAME)
public class TenantEntity {
    public static final String TENANT_FILTER_NAME = "tenantFilter";
    public  static final String TENANT_FILTER_ARGUMENT_NAME = "tenantId";
    public  static final String TENANT_ID_PROPERTY_NAME = "tenantId";

    @Column(nullable=false)
    @JsonIgnore
    private Long tenantId;

    @Version
    private Integer version;

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public Integer getVersion() {
        return this.version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    
}