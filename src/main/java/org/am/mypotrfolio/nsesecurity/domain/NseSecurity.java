package org.am.mypotrfolio.nsesecurity.domain;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NseSecurity {
    private String securityId;
    private String securityName;
    private String status;
    private String series;
    private String isin;
    private Double faceValue;
    private String industry;
    private String instrumentType;
    private String sectorName;
    private String industryNewName;
    private String industryGroupName;
    private String industrySubGroupName;
    private String sectorIndices;
    private String thematicIndices;
    private String marketIndices;
}