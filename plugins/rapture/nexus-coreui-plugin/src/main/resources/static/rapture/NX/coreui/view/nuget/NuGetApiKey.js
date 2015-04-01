/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
/*global Ext, NX*/

/**
 * NuGet repository settings form.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.nuget.NuGetApiKey', {
  extend: 'NX.view.SettingsPanel',
  alias: 'widget.nx-coreui-nuget-apikey',
  requires: [
    'NX.Conditions',
    'NX.util.Url'
  ],

  config: {
    active: false,
  },

  /**
   * @override
   */
  initComponent: function() {
    var me = this;

    me.items = [
      {
        xtype: 'form',
        ui: 'nx-subsection',

        items: [
          {
            xtype: 'label',
            // TODO - KR move this to pluginStrings.js
            html: '<p>A new API Key will be created the first time it is accessed.</p>' +
                '<p>Resetting your API Key will invalidate the current key.</p>'
          }
        ],

        buttonAlign: 'left',
        buttons: [
          { text: 'Access API Key', action: 'access', glyph: 'xf023@FontAwesome' /* fa-lock */, disabled: true },
          { text: 'Reset API Key', action: 'reset', ui: 'nx-danger', glyph: 'xf023@FontAwesome' /* fa-lock */, disabled: true }
        ]
      }
    ];

    me.callParent(arguments);
  }

});
