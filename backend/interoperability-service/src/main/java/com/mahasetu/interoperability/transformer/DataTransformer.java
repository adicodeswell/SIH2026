package com.mahasetu.interoperability.transformer;

import com.mahasetu.interoperability.model.CanonicalCitizenData;
import com.mahasetu.interoperability.model.RawExternalResponse;

public interface DataTransformer {
    CanonicalCitizenData transform(RawExternalResponse rawResponse);
}
