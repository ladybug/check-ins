package com.objectcomputing.checkins.util.googleApiAccess;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.admin.directory.Directory;
import com.google.api.services.drive.Drive;
import com.google.auth.http.HttpCredentialsAdapter;
import io.micronaut.context.annotation.Property;

import javax.inject.Singleton;
import java.io.IOException;
import java.security.GeneralSecurityException;

@Singleton
public class GoogleAccessor {

    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private final NetHttpTransport httpTransport;
    private final String applicationName;
    private GoogleAuthenticator authenticator;

    public GoogleAccessor(@Property(name = "check-ins.application.name") String applicationName,
                          GoogleAuthenticator authenticator) throws GeneralSecurityException, IOException {
        this.httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        this.applicationName = applicationName;
        this.authenticator = authenticator;
    }

    /**
     * Create and return the google drive access object
     *
     * @return a google drive access object
     * @throws IOException
     */
    public Drive accessGoogleDrive() throws IOException {
        HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(authenticator.setupCredentialsForDrive());
        return new Drive
                .Builder(httpTransport, JSON_FACTORY, requestInitializer)
                .setApplicationName(applicationName)
                .build();
    }

    /**
     * Create and return the google directory access object
     *
     * @return a google directory access object
     * @throws IOException
     */
    public Directory accessGoogleDirectory() throws IOException {
        return new Directory
                .Builder(httpTransport, JSON_FACTORY, authenticator.setupCredentialsForDirectory(httpTransport, JSON_FACTORY))
                .setApplicationName(applicationName)
                .build();
    }
}
