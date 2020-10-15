package com.objectcomputing.checkins.util.googleApiAccess;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.admin.directory.DirectoryScopes;
import com.google.auth.oauth2.GoogleCredentials;
import com.objectcomputing.checkins.security.GoogleServiceConfiguration;
import io.micronaut.context.annotation.Property;

import javax.inject.Singleton;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Singleton
public class GoogleAuthenticator {

    private final Collection<String> scopes;
    private GoogleServiceConfiguration gServiceConfig;

    /**
     * Creates a google drive utility for quick access
     *
     * @param scopes, the scope(s) of access to request for this application
     * @param gServiceConfig, Google Drive configuration properties
     */
    public GoogleAuthenticator(@Property(name = "check-ins.application.scopes") Collection<String> scopes,
                               GoogleServiceConfiguration gServiceConfig) {
        this.scopes = scopes;
        this.gServiceConfig = gServiceConfig;
    }

    /**
     * Creates an authorized Credential object.
     *
     * @return An authorized Credential object.
     * @throws IOException If the configured file cannot be found.
     */
    GoogleCredentials setupCredentialsForDrive() throws IOException {

        InputStream in = new ByteArrayInputStream(gServiceConfig.toString().getBytes(StandardCharsets.UTF_8));
        GoogleCredentials credentials = GoogleCredentials.fromStream(in);

        if (credentials == null) {
            credentials = GoogleCredentials.getApplicationDefault();
            throw new FileNotFoundException("Credentials not found while using Google default credentials");
        }

        return scopes.isEmpty() ? credentials : credentials.createScoped(scopes);
    }

    Credential setupCredentialsForDirectory(final NetHttpTransport HTTP_TRANSPORT, final JsonFactory JSON_FACTORY) throws IOException {

        String TOKENS_DIRECTORY_PATH = "tokens";
        List<String> SCOPES = Collections.singletonList(DirectoryScopes.ADMIN_DIRECTORY_USER_READONLY);

        // Load client secrets.
        InputStream in = new ByteArrayInputStream(gServiceConfig.toString().getBytes(StandardCharsets.UTF_8));
        if (in == null) {
            throw new FileNotFoundException("Credentials not found while using Google default credentials");
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }
}

