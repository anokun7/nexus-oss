/*
 * Copyright (c) 2008-2014 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/pro/attributions
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
/**
 * LDAP Server User & Group test results window.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.ldap.LdapServerUserAndGroupMappingTestResults', {
  extend: 'Ext.window.Window',
  alias: 'widget.nx-coreui-ldapserver-userandgroup-testresults',

  title: 'User Mapping Test Results',

  layout: 'fit',
  autoShow: true,
  modal: true,
  constrain: true,
  width: 630,

  buttonAlign: 'left',
  buttons: [
    { text: 'Close', handler: function () {
      this.up('window').close();
    }}
  ],

  /**
   * @cfg json array of users (as returned by checking the user mapping)
   */
  mappedUsers: undefined,

  initComponent: function () {
    var me = this;

    me.items = {
      xtype: 'grid',
      columns: [
        { header: 'User Id', dataIndex: 'username', flex: 1 },
        { header: 'Name', dataIndex: 'realName', flex: 1 },
        { header: 'Email', dataIndex: 'email', width: 250 },
        { header: 'Roles', dataIndex: 'membership', flex: 3 }
      ],
      store: Ext.create('Ext.data.JsonStore', {
        fields: ['username', 'realName', 'email', 'membership'],
        data: me.mappedUsers
      })
    };

    me.maxHeight = Ext.getBody().getViewSize().height - 100;

    me.callParent(arguments);
  }

});
