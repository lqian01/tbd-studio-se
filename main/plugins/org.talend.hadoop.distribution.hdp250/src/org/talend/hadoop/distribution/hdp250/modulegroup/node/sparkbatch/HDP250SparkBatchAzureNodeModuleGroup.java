// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.hadoop.distribution.hdp250.modulegroup.node.sparkbatch;

import java.util.HashSet;
import java.util.Set;

import org.talend.core.hadoop.version.EHadoopDistributions;
import org.talend.hadoop.distribution.DistributionModuleGroup;
import org.talend.hadoop.distribution.condition.common.SparkBatchLinkedNodeCondition;
import org.talend.hadoop.distribution.constants.SparkBatchConstant;
import org.talend.hadoop.distribution.hdp250.HDP250Constant;
import org.talend.hadoop.distribution.hdp250.HDP250Distribution;

public class HDP250SparkBatchAzureNodeModuleGroup {

    public static Set<DistributionModuleGroup> getModuleGroups() {
        Set<DistributionModuleGroup> hs = new HashSet<>();
        DistributionModuleGroup dmg = new DistributionModuleGroup(
                HDP250Constant.SPARK_AZURE_MRREQUIRED_MODULE_GROUP.getModuleName(), true, new SparkBatchLinkedNodeCondition(
                        EHadoopDistributions.HORTONWORKS.getName(), HDP250Distribution.VERSION,
                        SparkBatchConstant.SPARK_BATCH_AZURE_SPARKCONFIGURATION_LINKEDPARAMETER).getCondition());

        hs.add(dmg);
        return hs;
    }
}
