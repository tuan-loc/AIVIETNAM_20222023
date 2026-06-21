package com.example.gradepredict;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface GradeService {

    @FormUrlEncoded
    @POST("/predict")
    Call<Result> getGrade(@Field("cgpa") String cpga, @Field("iq") String iq, @Field("profile_score") String profileScore);
}
