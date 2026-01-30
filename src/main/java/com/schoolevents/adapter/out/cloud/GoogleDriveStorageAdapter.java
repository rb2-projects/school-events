package com.schoolevents.adapter.out.cloud;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.schoolevents.domain.port.out.StoragePort;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

public class GoogleDriveStorageAdapter implements StoragePort {

    private final Drive driveService;
    private final String folderId;

    public GoogleDriveStorageAdapter(String credentialsFilePath, String folderId)
            throws IOException, GeneralSecurityException {
        this.folderId = folderId;
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

        GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(credentialsFilePath))
                .createScoped(Collections.singleton(DriveScopes.DRIVE_FILE));
        HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);

        this.driveService = new Drive.Builder(GoogleNetHttpTransport.newTrustedTransport(), jsonFactory,
                requestInitializer)
                .setApplicationName("School Events Organizer")
                .build();
    }

    @Override
    public void upload(String fileName, byte[] content) throws IOException {
        // Check if file exists to update it, otherwise create new
        String fileId = findFileId(fileName);

        File fileMetadata = new File();
        fileMetadata.setName(fileName);

        ByteArrayContent mediaContent = new ByteArrayContent("application/json", content);

        if (fileId != null) {
            // Update existing file
            driveService.files().update(fileId, fileMetadata, mediaContent).execute();
            System.out.println("Updated file on Google Drive: " + fileName + " (ID: " + fileId + ")");
        } else {
            // Create new file
            fileMetadata.setParents(Collections.singletonList(folderId));
            File file = driveService.files().create(fileMetadata, mediaContent)
                    .setFields("id")
                    .execute();
            System.out.println("Created file on Google Drive: " + fileName + " (ID: " + file.getId() + ")");
        }
    }

    private String findFileId(String fileName) throws IOException {
        String query = "name = '" + fileName + "' and '" + folderId + "' in parents and trashed = false";
        List<File> files = driveService.files().list()
                .setQ(query)
                .setSpaces("drive")
                .setFields("files(id, name)")
                .execute()
                .getFiles();

        if (files != null && !files.isEmpty()) {
            return files.get(0).getId();
        }
        return null; // Not found
    }
}
