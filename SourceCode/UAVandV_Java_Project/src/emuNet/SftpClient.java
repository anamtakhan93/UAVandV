package emuNet;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

public class SftpClient {
	
	//private static final String username = "tjcruz";
	//private static final String password = "Bubbles%21";
	//private static final String host = "10.254.0.166";
	

	private ChannelSftp setupJsch(String username, String password, String host) throws JSchException {
		
		JSch jsch = new JSch();
		Session jschSession = jsch.getSession(username, host);
		java.util.Properties config = new java.util.Properties();
		config.put("StrictHostKeyChecking", "no");
		jschSession.setConfig(config);
		jschSession.setPassword(password);
		jschSession.connect();
		return (ChannelSftp) jschSession.openChannel("sftp");
	}

	/**
	 * Upload a file via SFTP
	 * @param localFile - Path of the local file
	 * @param sftpFile - Destination Path
	 * @return
	 */
	public boolean uploadSftpFromPath(String localFile, String sftpFile, String username, String password, String host) {
		ChannelSftp channelSftp = null;
		try {
			channelSftp = setupJsch(username, password, host);
		} catch (JSchException e) {
			// throw the exception
		}
		try {
			channelSftp.connect();
		} catch (JSchException e) {
			// throw the exception
		}
		try {
			channelSftp.put(localFile, sftpFile);
			//System.out.println("Upload Complete");
		} catch (SftpException e) {
			// throw the exception
		}
		channelSftp.exit();
		return true;
	}
}
