package server;

import utils.Utils;

public final class SFTPCommands {
    private SFTPCommands() {
        throw new IllegalAccessError("server.SFTPCommands cannot be instantiated");
    }

    public static String cdir(SFTPServer.SFTPClientWorker client, String destDir) {
        // signal the client must log in before cdir can happen
        if (!client.isLoggedIn()) {
            client.setPendingDirChange(destDir);
            return makeResponse("Directory exists, send account/password", ResponseCode.Success);
        }
        // resolve selected directory
        String selectedCDir;
        if (destDir.equals("/")) {
            selectedCDir = client.getSelectedUser().getRootDir();
        } else if (destDir.startsWith("/")) {
            selectedCDir = client.getSelectedUser().getId() + destDir;
        } else {
            selectedCDir = Utils.appendIfMissing(client.getCurrentDir(), "/");
            selectedCDir += destDir;
        }
        // validate dir can be navigated to and set currentDir if applicable
        if (!FileSystem.dirExists(selectedCDir)) {
            return makeResponse("Cant connect to directory because: " + selectedCDir  + " does not exist", ResponseCode.Error);
        } else if (FileSystem.pathIsFile(selectedCDir)) {
            return makeResponse("Cant list directory because: " + selectedCDir  + " is not a directory", ResponseCode.Error);
        }
        // change current directory
        client.setCurrentDir(selectedCDir);
        return makeResponse("Changed working dir to " + client.getCurrentDir(), ResponseCode.LoggedIn);
    }

    private static String makeResponse(String msg, ResponseCode responseCode) {
		return responseCode.toString() + msg + '\0';
    }
}
