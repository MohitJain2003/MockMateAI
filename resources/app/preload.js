const { contextBridge } = require('electron');

// Expose minimal, safe APIs to the renderer process
// No Node.js APIs are directly exposed — all communication
// goes through this controlled bridge
contextBridge.exposeInMainWorld('electronAPI', {
    platform: process.platform
});
