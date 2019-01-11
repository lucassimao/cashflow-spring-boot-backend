package com.lucassimao.fluxodecaixa.model;

import static com.lucassimao.fluxodecaixa.model.TenantEntity.TENANT_FILTER_ARGUMENT_NAME;
import static com.lucassimao.fluxodecaixa.model.TenantEntity.TENANT_FILTER_NAME;
import static com.lucassimao.fluxodecaixa.model.TenantEntity.TENANT_ID_PROPERTY_NAME;

import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.MappedSuperclass;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

@MappedSuperclass()
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@FilterDef(
  name = TENANT_FILTER_NAME, 
  parameters = @ParamDef(name = TENANT_FILTER_ARGUMENT_NAME, type = "long"), 
  defaultCondition = TENANT_ID_PROPERTY_NAME + "= :" + TENANT_FILTER_ARGUMENT_NAME)
@Filter(name = TENANT_FILTER_NAME)
public class TenantEntity {
    public static final String TENANT_FILTER_NAME = "tenantFilter";
    public  static final String TENANT_ID_PROPERTY_NAME = "tenant_id";
    public  static final String TENANT_FILTER_ARGUMENT_NAME = "tenantId";

    private Long tenantId;

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }
}