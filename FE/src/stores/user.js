import { ref, computed } from "vue";
import { defineStore } from "pinia";
import router from "@/router";
import axios from "axios";

export const useUserStore = defineStore("user", () => {
  // =========== STATE ===============

  const user = ref({});
  const userId = ref("");
  const userName = ref("");

  const userRDI = ref({}); // RDI - Recommended Daily Intake (당일 권장량)
  const userRDICaffeine = ref(0.0);
  const userRDISugar = ref(0.0);

  const userMax = ref({});
  const userMaxCaffeine = ref(0.0);
  const userMaxCaffeineDate = ref("");
  const userMaxSugar = ref(0.0);
  const userMaxSugarDate = ref("");

  const recommendedCaffeine = ref({});
  const recommendedSugar = ref({});

  // =========== GETTER ===============

  const getUser = computed(() => {
    return user.value;
  });

  const getUserId = computed(() => {
    return userId.value;
  });

  const getUserName = computed(() => {
    return userName.value;
  });

  const getUserRDI = computed(() => {
    return userRDI.value;
  });

  const getUserRDICaffeine = computed(() => {
    return userRDICaffeine.value;
  });

  const getUserRDISugar = computed(() => {
    return userRDISugar.value;
  });

  const getUserMax = computed(() => {
    return userMax.value;
  });

  const getUserMaxCaffeine = computed(() => {
    return userMaxCaffeine.value;
  });

  const getUserMaxCaffeineDate = computed(() => {
    return userMaxCaffeineDate.value;
  });

  const getUserMaxSugar = computed(() => {
    return userMaxSugar.value;
  });

  const getUserMaxSugarDate = computed(() => {
    return userMaxSugarDate.value;
  });

  const getRecommendedCaffeine = computed(() => {
    return recommendedCaffeine.value;
  });

  const getRecommendedSugar = computed(() => {
    return recommendedSugar.value;
  });

  // =========== ACTION ===============

  const sendKakaoToken = function (token) {
    axios({
      url: `${import.meta.env.VITE_REST_USER_API}/social-login`,
      method: "POST",
      data: user,
    })
      .then(() => {
        // 카카오 에서 받아온 토큰을 백으로 전달
      })
      .catch((err) => {
        console.log(err);
      });
  };

  const createUser = function (user) {
    axios({
      url: import.meta.env.VITE_REST_USER_API,
      method: "POST",
      data: user,
    })
      .then(() => {})
      .catch((err) => {
        console.log(err);
      });
  };

  const researchUser = function () {
    axios({
      url: import.meta.env.VITE_REST_USER_API,
      method: "GET",
    })
      .then((res) => {
        user.value = res.data;
      })
      .catch((err) => {
        console.log(err);
      });
  };

  const researchAmount = function () {
    axios({
      url: `${import.meta.env.VITE_REST_USER_API}/amount`,
      method: "GET",
    })
      .then((res) => {
        userRDI.value = res.data;
        userRDICaffeine.value = userRDI.value.userCaffeine;
        userRDISugar.value = userRDI.value.userSugar;
      })
      .catch((err) => {
        console.log(err);
      });
  };

  const researchMax = function () {
    axios({
      url: `${import.meta.env.VITE_REST_USER_API}/max`,
      method: "GET",
    })
      .then((res) => {
        userMax.value = res.data;
        userMaxCaffeine.value = userMax.value.maxCaffeineValue;
        userMaxCaffeineDate.value = userMax.value.maxCaffeineDate;
        userMaxSugar.value = userMax.value.maxSugarValue;
        userMaxSugarDate.value = userMax.value.maxSugarDate;
      })
      .catch((err) => {
        console.log(err);
      });
  };

  const updateUser = function (updateData) {
    axios({
      url: import.meta.env.VITE_REST_USER_API,
      method: "PUT",
      data: updateData, 
    })
    .then(() => {
      alert('사용자 정보가 성공적으로 업데이트되었습니다.');
      router.push('/mypage'); 
    })
    .catch((err) => {
      console.error("Error updating user:", err);
    });
  };
  

  const deleteUser = function () {
    axios({
      url: import.meta.env.VITE_REST_USER_API,
      method: "DELETE",
    })
      .then(() => {})
      .catch((err) => {
        console.log(err);
      });
  };

  const researchRecommendSugar = function () {
    axios({
      url: `${import.meta.env.VITE_REST_USER_API}/recommendsugar`,
      method: "GET",
    })
      .then((res) => {
        recommendedSugar.value = res.data;
      })
      .catch((err) => {
        console.log(err);
      });
  };

  const researchRecommendCaffeine = function () {
    axios({
      url: `${import.meta.env.VITE_REST_USER_API}/recommendcaffeine`,
      method: "GET",
    })
      .then((res) => {
        recommendedCaffeine.value = res.data;
      })
      .catch((err) => {
        console.log(err);
      });
  };
  
  const infoFilled = computed(() => {     
    const userInfo = user.value;   
    if (!userInfo) {
      return false;
    }
    // 모든 필요한 속성이 존재하는지 확인
    return userInfo.hasOwnProperty('userHeight') && userInfo.userHeight !== 0 &&
           userInfo.hasOwnProperty('userWeight') && userInfo.userWeight !== 0 &&
           userInfo.hasOwnProperty('userHealth') && userInfo.userHealth !== 0;
  });
  
  

  return {
    user,
    userId,
    userName,
    userRDI,
    userRDICaffeine,
    userRDISugar,
    userMax,
    userMaxCaffeine,
    userMaxCaffeineDate,
    userMaxSugar,
    userMaxSugarDate,
    recommendedCaffeine,
    recommendedSugar,
    getUser,
    getUserId,
    getUserName,
    getUserRDI,
    getUserRDICaffeine,
    getUserRDISugar,
    getUserMax,
    getUserMaxCaffeine,
    getUserMaxCaffeineDate,
    getUserMaxSugar,
    getUserMaxSugarDate,
    getRecommendedCaffeine,
    getRecommendedSugar,
    sendKakaoToken,
    createUser,
    researchUser,
    researchAmount,
    researchMax,
    updateUser,
    deleteUser,
    researchRecommendSugar,
    researchRecommendCaffeine,
    infoFilled,
  };
});
