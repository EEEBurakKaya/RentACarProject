package com.etiya.rentACarSpring.core.utilities.adapter.posServiceAdapter;

import com.etiya.rentACarSpring.businnes.request.PosServiceRequest;

public interface posSystemService {
     boolean checkPayment(PosServiceRequest posServiceRequest);
}
