/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package org.glassfish.admingui.devtests;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 *
 * @author anilam
 */
public class ClusterTest extends BaseSeleniumTestClass {
    public static final String TRIGGER_CLUSTER_EDIT = "General Information";
    public static final String TRIGGER_MIGRATE_EJB_TIMERS = "Migrate EJB timers associated with a server";
    public static final String TRIGGER_CLUSTER_PAGE = "Clusters (";

    final String TRIGGER_NEW_PAGE = "Server Instances to be Created";

    final String TRIGGER_CLUSTER_GENERAL_PAGE = "Status:";

    final String TRIGGER_CLUSTER_INSTANCE_NEW_PAGE = "Node:";

    final String TRIGGER_CLUSTER_INSTANCES_PAGE = "Server Instances (";
    final String TRIGGER_CLUSTER_RESOURCES_PAGE = "All instances in a cluster have the same set of resources, resulting in the same JNDI namespace.";

    @Test
    public void testCreateClusterWithOneInstance() {
        String clusterName = "cluster" + generateRandomString();
        String instanceName = "instanceName" + generateRandomString();

        createCluster(clusterName, instanceName);
        assertTrue(selenium.isTextPresent(clusterName));

        String prefix = getTableRowByValue("propertyForm:clustersTable", clusterName, "col1");
        assertEquals(clusterName, selenium.getText(prefix + "col1:link"));
        assertEquals(clusterName + "-config", selenium.getText(prefix + "col2:configlink"));
        assertEquals(instanceName, selenium.getText(prefix + "col3:iLink"));
        deleteRow("propertyForm:clustersTable:topActionsGroup1:button1", "propertyForm:clustersTable", clusterName);
    }

    @Test
    public void testStartAndStopClusterWithOneInstance() {
        String clusterName = "clusterName" + generateRandomString();
        String instanceName1 = "instanceName" + generateRandomString();

        createCluster(clusterName, instanceName1);
        assertTrue(selenium.isTextPresent(clusterName));

        rowActionWithConfirm("propertyForm:clustersTable:topActionsGroup1:button2", "propertyForm:clustersTable", clusterName);
        waitForCondition("document.getElementById('propertyForm:clustersTable:topActionsGroup1:button2').value != 'Processing...'", 300000);
        String prefix = getTableRowByValue("propertyForm:clustersTable", clusterName, "col1");
        assertTrue((selenium.getText(prefix + "col3").indexOf("Running") != -1));
        rowActionWithConfirm("propertyForm:clustersTable:topActionsGroup1:button3", "propertyForm:clustersTable", clusterName);
        waitForCondition("document.getElementById('propertyForm:clustersTable:topActionsGroup1:button3').value != 'Processing...'", 300000);
        assertTrue((selenium.getText(prefix + "col3").indexOf("Stopped") != -1));
        deleteRow("propertyForm:clustersTable:topActionsGroup1:button1", "propertyForm:clustersTable", clusterName);

    }

    @Test
    public void testClusterGeneralPage() {
        String clusterName = "cluster" + generateRandomString();
        String instanceName = "instanceName" + generateRandomString();

        createCluster(clusterName, instanceName);
        assertTrue(selenium.isTextPresent(clusterName));
        clickAndWait(getLinkIdByLinkText("propertyForm:clustersTable", clusterName), TRIGGER_CLUSTER_GENERAL_PAGE);
        assertEquals(clusterName, selenium.getText("propertyForm:propertySheet:propertSectionTextField:clusterNameProp:clusterName"));

        //ensure config link is fine.``
        //TODO:  how to ensure thats the correct configuration page ?
        assertEquals(clusterName + "-config", selenium.getText("propertyForm:propertySheet:propertSectionTextField:configNameProp:configlink"));
        clickAndWait("propertyForm:propertySheet:propertSectionTextField:configNameProp:configlink", "Admin Service");

        //Back to the Clusters page,  ensure default value is there.
        clickAndWait("treeForm:tree:clusterTreeNode:clusterTreeNode_link", TRIGGER_CLUSTER_PAGE);
        clickAndWait(getLinkIdByLinkText("propertyForm:clustersTable", clusterName), TRIGGER_CLUSTER_GENERAL_PAGE);
        assertEquals("0 instance(s) running", selenium.getText("propertyForm:propertySheet:propertSectionTextField:instanceStatusProp:instanceStatusRunning"));
        assertEquals("1 instance(s) not running", selenium.getText("propertyForm:propertySheet:propertSectionTextField:instanceStatusProp:instanceStatusStopped"));

        //change value
        selenium.type("propertyForm:propertySheet:propertSectionTextField:gmsMulticastPort:gmsMulticastPort", "12345");
        selenium.type("propertyForm:propertySheet:propertSectionTextField:gmsMulticastAddress:gmsMulticastAddress", "123.234.456.88");
        selenium.type("propertyForm:propertySheet:propertSectionTextField:GmsBindInterfaceAddress:GmsBindInterfaceAddress", "${ABCDE}");
        selenium.click("propertyForm:propertySheet:propertSectionTextField:gmsEnabledProp:gmscb");
        clickAndWait("propertyForm:propertyContentPage:topButtons:saveButton", MSG_NEW_VALUES_SAVED);

        //ensure value is saved correctly
        assertEquals("12345", selenium.getValue("propertyForm:propertySheet:propertSectionTextField:gmsMulticastPort:gmsMulticastPort"));
        assertEquals("123.234.456.88", selenium.getValue("propertyForm:propertySheet:propertSectionTextField:gmsMulticastAddress:gmsMulticastAddress"));
        assertEquals("${ABCDE}", selenium.getValue("propertyForm:propertySheet:propertSectionTextField:GmsBindInterfaceAddress:GmsBindInterfaceAddress"));
        assertEquals("off", selenium.getValue("propertyForm:propertySheet:propertSectionTextField:gmsEnabledProp:gmscb"));
        clickAndWait("treeForm:tree:clusterTreeNode:clusterTreeNode_link", TRIGGER_CLUSTER_PAGE);
        deleteRow("propertyForm:clustersTable:topActionsGroup1:button1", "propertyForm:clustersTable", clusterName);
    }

    @Test
    public void testMultiDeleteClusters() {
        String clusterName1 = "cluster" + generateRandomString();
        String clusterName2 = "cluster" + generateRandomString();

        createCluster(clusterName1);
        createCluster(clusterName2);
        selenium.click("propertyForm:clustersTable:_tableActionsTop:_selectMultipleButton:_selectMultipleButton_image");
        //selenium.chooseOkOnNextConfirmation();
        selenium.click("propertyForm:clustersTable:topActionsGroup1:button1");
        if (selenium.isConfirmationPresent()) {
            selenium.getConfirmation();
        }
        waitForCondition("document.getElementById('propertyForm:clustersTable:topActionsGroup1:button1').value != 'Processing...'", 300000);
        sleep(10000);
        assertFalse(selenium.isTextPresent(clusterName1));
        assertFalse(selenium.isTextPresent(clusterName2));

    }

    @Test
    public void testClusterInstancesTab() {
        String clusterName = "cluster" + generateRandomString();
        String instanceName = "instanceName" + generateRandomString();
        String instanceName2 = "instanceName" + generateRandomString();

        createCluster(clusterName, instanceName);
        assertTrue(selenium.isTextPresent(clusterName));
        clickAndWait(getLinkIdByLinkText("propertyForm:clustersTable", clusterName), TRIGGER_CLUSTER_GENERAL_PAGE);
        clickAndWait("propertyForm:clusterTabs:clusterInst", TRIGGER_CLUSTER_INSTANCES_PAGE);
        assertTrue(selenium.isTextPresent(instanceName));

        clickAndWait("propertyForm:instancesTable:topActionsGroup1:newButton", TRIGGER_CLUSTER_INSTANCE_NEW_PAGE);
        selenium.type("propertyForm:propertySheet:propertSectionTextField:NameTextProp:NameText", instanceName2);
        clickAndWait("propertyForm:propertyContentPage:topButtons:newButton", TRIGGER_CLUSTER_INSTANCES_PAGE);
        assertTrue(selenium.isTextPresent(instanceName2));

        clickAndWait("treeForm:tree:clusterTreeNode:clusterTreeNode_link", TRIGGER_CLUSTER_PAGE);
        deleteRow("propertyForm:clustersTable:topActionsGroup1:button1", "propertyForm:clustersTable", clusterName);
    }

    @Test
    public void testMigrateEjbTimers() {
        final String clusterName = "cluster" + generateRandomString();
        final String instanceName1 = "instanceName" + generateRandomString();
        final String instanceName2 = "instanceName" + generateRandomString();
        final String instanceName3 = "instanceName" + generateRandomString();

        createCluster(clusterName, instanceName1, instanceName2,instanceName3);

        assertTrue(selenium.isTextPresent(clusterName));
        clickAndWait(getLinkIdByLinkText("propertyForm:clustersTable", clusterName), TRIGGER_CLUSTER_GENERAL_PAGE);
        clickAndWait("propertyForm:clusterTabs:clusterInst", TRIGGER_CLUSTER_INSTANCES_PAGE);
        assertTrue(selenium.isTextPresent(instanceName1));
        assertTrue(selenium.isTextPresent(instanceName2));
        assertTrue(selenium.isTextPresent(instanceName3));

        this.selectTableRowByValue("propertyForm:instancesTable", instanceName1);

//        this.rowActionWithConfirm("propertyForm:instancesTable:topActionsGroup1:button2", "propertyForm:instancesTable", "Running");
        selenium.chooseOkOnNextConfirmation();
//        selectTableRowByValue("propertyForm:instancesTable", instanceName1, "col0", "col1");
        selenium.click("propertyForm:instancesTable:topActionsGroup1:button2");
        if (selenium.isConfirmationPresent()) {
            selenium.getConfirmation();
        }
        waitForPageLoad("Running", 120);


        clickAndWait("propertyForm:clusterTabs:general", TRIGGER_CLUSTER_EDIT);
        clickAndWait("propertyForm:migrateTimesButton", TRIGGER_MIGRATE_EJB_TIMERS);
        selenium.select("propertyForm:propertySheet:propertSectionTextField:clusterSourceProp:source", "label="+instanceName2);
        selenium.select("propertyForm:propertySheet:propertSectionTextField:clusterDestProp:dest", "label="+instanceName1);

        clickAndWait("propertyForm:propertyContentPage:topButtons:saveButton", instanceName1 + " : Migrated 0 timers from " + instanceName2 + " to " + instanceName1 + ".");
        clickAndWait("propertyForm:clusterTabs:clusterInst", TRIGGER_CLUSTER_INSTANCES_PAGE);

        selenium.click("propertyForm:instancesTable:rowGroup1:1:col0:select");
        selenium.click("propertyForm:instancesTable:topActionsGroup1:button3");
        if (selenium.isConfirmationPresent()) {
            selenium.getConfirmation();
        }
        waitForPageLoad("Running", 120, true);
        selenium.click("propertyForm:instancesTable:_tableActionsTop:_selectMultipleButton:_selectMultipleButton_image");

        selenium.click("propertyForm:instancesTable:topActionsGroup1:button1");
        if (selenium.isConfirmationPresent()) {
            selenium.getConfirmation();
        }
        waitForPageLoad("Server Instances (0)", TIMEOUT);
        clickAndWait("propertyForm:clusterTabs:general", TRIGGER_CLUSTER_EDIT);

        selenium.click("treeForm:tree:clusterTreeNode:clusterTreeNode_link");
        deleteRow("propertyForm:clustersTable:topActionsGroup1:button1", "propertyForm:clustersTable", clusterName);
    }

    @Test
    public void propertiesTest() {
        final String clusterName = "cluster" + generateRandomString();
        final String instanceName1 = "instanceName" + generateRandomString();
        final String instanceName2 = "instanceName" + generateRandomString();
        final String instanceName3 = "instanceName" + generateRandomString();

        //treeForm:tree:clusterTreeNode:c1:link
        createCluster(clusterName, instanceName1, instanceName2,instanceName3);
        assertTrue(selenium.isTextPresent(clusterName));
        clickAndWait(getLinkIdByLinkText("propertyForm:clustersTable", clusterName), TRIGGER_CLUSTER_GENERAL_PAGE);
        assertEquals(clusterName, selenium.getText("propertyForm:propertySheet:propertSectionTextField:clusterNameProp:clusterName"));

        // Go to properties tab
        clickAndWait("propertyForm:clusterTabs:clusterProps", "Cluster System Properties");
        int sysPropCount = addTableRow("propertyForm:basicTable", "propertyForm:basicTable:topActionsGroup1:addSharedTableButton");
        selenium.type("propertyForm:basicTable:rowGroup1:0:col2:col1St", "property"+generateRandomString());
        selenium.type("propertyForm:basicTable:rowGroup1:0:col3:col1St", "value");
        clickAndWait("propertyForm:propertyContentPage:topButtons:saveButton", MSG_NEW_VALUES_SAVED);

        // Go to cluster props page
        selenium.click("propertyForm:clusterTabs:clusterProps:clusterInstanceProps");
        waitForPageLoad("Cluster System Properties", TIMEOUT, true);

        int clusterPropCount = addTableRow("propertyForm:basicTable", "propertyForm:basicTable:topActionsGroup1:addSharedTableButton");
        selenium.type("propertyForm:basicTable:rowGroup1:0:col2:col1St", "property"+generateRandomString());
        selenium.type("propertyForm:basicTable:rowGroup1:0:col3:col1St", "value");
        clickAndWait("propertyForm:propertyContentPage:topButtons:saveButton", MSG_NEW_VALUES_SAVED);

        // Verify that properties were persisted
        clickAndWait("propertyForm:clusterTabs:clusterProps:clusterSystemProps", "Cluster System Properties");
        assertTableRowCount("propertyForm:basicTable", sysPropCount);
        selenium.click("propertyForm:clusterTabs:clusterProps:clusterInstanceProps");
        waitForPageLoad("Cluster System Properties", TIMEOUT, true);
        assertTableRowCount("propertyForm:basicTable", clusterPropCount);

        clickAndWait("treeForm:tree:clusterTreeNode:clusterTreeNode_link", TRIGGER_CLUSTER_PAGE);
        deleteRow("propertyForm:clustersTable:topActionsGroup1:button1", "propertyForm:clustersTable", clusterName);
    }

    public void testClusterResourcesPage() {
        final String jndiName = "jdbcResource" + generateRandomString();
        String target = "cluster" + generateRandomString();
        final String description = "devtest test for cluster->resources page- " + jndiName;
        final String tableID = "propertyForm:resourcesTable";

        JdbcTest jdbcTest = new JdbcTest();
        jdbcTest.createJDBCResource(jndiName, description, target, MonitoringTest.TARGET_CLUSTER_TYPE);

        clickAndWait("treeForm:tree:clusterTreeNode:clusterTreeNode_link", TRIGGER_CLUSTER_PAGE);
        clickAndWait(getLinkIdByLinkText("propertyForm:clustersTable", target), TRIGGER_CLUSTER_GENERAL_PAGE);
        clickAndWait("propertyForm:clusterTabs:clusterResources", TRIGGER_CLUSTER_RESOURCES_PAGE);
        assertTrue(selenium.isTextPresent(jndiName));

        int jdbcCount = getTableRowCountByValue(tableID, "JDBC Resources", "col3:type");
        int customCount = getTableRowCountByValue(tableID, "Custom Resources", "col3:type");

        EnterpriseServerTest adminServerTest = new EnterpriseServerTest();
        selenium.select("propertyForm:resourcesTable:topActionsGroup1:filter_list", "label=Custom Resources");
        adminServerTest.waitForTableRowCount(tableID, customCount);

        selenium.select("propertyForm:resourcesTable:topActionsGroup1:filter_list", "label=JDBC Resources");
        adminServerTest.waitForTableRowCount(tableID, jdbcCount);

        selectTableRowByValue("propertyForm:resourcesTable", jndiName);
        waitForButtonEnabled("propertyForm:resourcesTable:topActionsGroup1:button1");
        selenium.click("propertyForm:resourcesTable:topActionsGroup1:button1");
        waitForButtonDisabled("propertyForm:resourcesTable:topActionsGroup1:button1");

        /*selenium.select("propertyForm:resourcesTable:topActionsGroup1:actions", "label=JDBC Resources");
        waitForPageLoad(JdbcTest.TRIGGER_NEW_JDBC_RESOURCE, true);
        clickAndWait("form:propertyContentPage:topButtons:cancelButton", JdbcTest.TRIGGER_JDBC_RESOURCES);*/

        jdbcTest.deleteJDBCResource(jndiName, target, MonitoringTest.TARGET_CLUSTER_TYPE);
    }

    public void createCluster(String clusterName, String... instanceNames) {
        clickAndWait("treeForm:tree:clusterTreeNode:clusterTreeNode_link", TRIGGER_CLUSTER_PAGE);
        clickAndWait("propertyForm:clustersTable:topActionsGroup1:newButton", TRIGGER_NEW_PAGE);
        selenium.type("propertyForm:propertySheet:propertSectionTextField:NameTextProp:NameText", clusterName);
        if (instanceNames != null) {
            for (String instanceName : instanceNames) {
                if (instanceName != null && !instanceName.equals("")) {
                    addTableRow("propertyForm:basicTable", "propertyForm:basicTable:topActionsGroup1:addSharedTableButton", "Server Instances to be Created");
                    selenium.type("propertyForm:basicTable:rowGroup1:0:col2:name", instanceName);
                }
            }
        }
        clickAndWait("propertyForm:propertyContentPage:topButtons:newButton", TRIGGER_CLUSTER_PAGE);
    }
}