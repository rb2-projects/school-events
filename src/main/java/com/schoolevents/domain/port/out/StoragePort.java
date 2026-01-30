package com.schoolevents.domain.port.out;

import java.io.IOException;

public interface StoragePort {
    void upload(String fileName, byte[] content) throws IOException;
}
