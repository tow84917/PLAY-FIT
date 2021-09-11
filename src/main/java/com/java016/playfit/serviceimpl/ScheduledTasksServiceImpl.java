package com.java016.playfit.serviceimpl;

import java.util.Date;
import java.util.List;

import com.java016.playfit.dao.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.java016.playfit.model.Avatar;
import com.java016.playfit.model.BodyType;
import com.java016.playfit.model.DailyRecord;
import com.java016.playfit.model.HealthRecord;
import com.java016.playfit.model.User;
import com.java016.playfit.service.AvatarService;
import com.java016.playfit.service.DailyRecordService;
import com.java016.playfit.service.HealthRecordService;
import com.java016.playfit.service.ScheduledTasksService;
import com.java016.playfit.service.UserService;
import com.java016.playfit.tool.BodyCalculator;

@Service
public class ScheduledTasksServiceImpl implements ScheduledTasksService {

	@Autowired
	DailyRecordService dailyRecordService;

	@Autowired
	HealthRecordService healthRecordService;

	@Autowired
	UserService userService;

	@Autowired
	BodyCalculator bodyCalculator;
	
	@Autowired
	AvatarService avatarService;

	@Autowired
	UserRepository userRepository;

	/**
	 * 檢查熱量赤字
	 */
	@Override
	@Scheduled(cron = "0 0 0 * * ?") // 指定時間執行 0時(24)
	public void upadteCalorieDeficit() {

		long now = System.currentTimeMillis();
		int dayMillis = 86400000;

		// 昨天的日期 util
		java.util.Date utilYestoday = new java.util.Date(now - dayMillis);
		// 把日期轉成SQL型態的Date
		java.sql.Date sqlYestoday = new java.sql.Date(utilYestoday.getTime());

		System.out.println(sqlYestoday);

		// 找昨天所有紀錄
		List<DailyRecord> dailyRecords = dailyRecordService.findByCreatedDate(sqlYestoday);

//		dailyRecords.forEach(d -> {
//			System.out.println(d.getUser().getId() + " : " 
//							 + d.getKcalBurned()   + " : " 
//							 + d.getKcalIntake()
//							 );
//		});

		for (DailyRecord dr : dailyRecords) {
			// 此紀錄主人
			User tempDrUser = dr.getUser();
			
			// 找 User "最近"健康紀錄
			HealthRecord healthRecordLast = 
					healthRecordService.findLastDateByUserId(tempDrUser.getId());
			// 昨日熱量赤字
			double calorieDeficitYestoday = 
					bodyCalculator.calCalorieDeficit(healthRecordLast, dr);
			
			// 更新後的熱量赤字
			double calorieDeficitUpdated = 
					healthRecordLast.getCalorieDeficit() + calorieDeficitYestoday ;
//			System.out.println(healthRecordLastUpdated.getCalorieDeficit());
			
			// 達成"減"一公斤
			if (calorieDeficitUpdated <= -7700) {
				// 找user 昨天是否有紀錄
				HealthRecord healthRecordYestoday = 
						healthRecordService.findByUserIdAndDate(tempDrUser.getId(),
						sqlYestoday);
				// 無紀錄則創建
				if (healthRecordYestoday == null) {
					healthRecordService.createNewRecord(
							healthRecordLast, tempDrUser, sqlYestoday);
					// 再取昨天紀錄(剛創好的)
					HealthRecord healthRecordYestodayNew = 
							healthRecordService.findByUserIdAndDate(tempDrUser.getId(),
							sqlYestoday);
					// 消除熱量赤字 7700
					healthRecordYestodayNew.setCalorieDeficit(calorieDeficitUpdated + 7700);
					// 減一公斤
					healthRecordYestodayNew.setWeight(
							healthRecordYestodayNew.getWeight() - 1);
					// 更新紀錄並儲存
					healthRecordService.updateHealthRecord(tempDrUser, healthRecordYestodayNew);
				}

				// 有紀錄則更新
				if (healthRecordYestoday != null) {
					// 消除熱量赤字 7700
					healthRecordYestoday.setCalorieDeficit(calorieDeficitUpdated + 7700);
					// 減一公斤
					healthRecordYestoday.setWeight(healthRecordYestoday.getWeight() - 1);
					healthRecordService.updateHealthRecord(tempDrUser, healthRecordYestoday);
				}
			}

			// 達成"加"一公斤
			if (calorieDeficitUpdated >= 7700) {
				// 找user 昨天是否有紀錄
				HealthRecord healthRecordYestoday = 
						healthRecordService.findByUserIdAndDate(tempDrUser.getId(),
						sqlYestoday);
				// 無紀錄則創建
				if (healthRecordYestoday == null) {
					healthRecordService.createNewRecord(
							healthRecordLast, tempDrUser, sqlYestoday);
					// 再取昨天紀錄(剛創好的)
					HealthRecord healthRecordYestodayNew = 
							healthRecordService.findByUserIdAndDate(tempDrUser.getId(),
							sqlYestoday);
					// 消除熱量赤字 7700
					healthRecordYestodayNew.setCalorieDeficit(calorieDeficitUpdated - 7700);
					// 減一公斤
					healthRecordYestodayNew.setWeight(healthRecordYestodayNew.getWeight() + 1);
					// 更新紀錄並儲存
					healthRecordService.updateHealthRecord(tempDrUser, healthRecordYestodayNew);
				}

				// 有紀錄則更新
				if (healthRecordYestoday != null) {
					// 消除熱量赤字 7700
					healthRecordYestoday.setCalorieDeficit(calorieDeficitUpdated - 7700);
					// 減一公斤
					healthRecordYestoday.setWeight(healthRecordYestoday.getWeight() + 1);
					healthRecordService.updateHealthRecord(tempDrUser, healthRecordYestoday);
				}
			}
			
			// 未達熱量赤字增減值
			if (calorieDeficitUpdated > -7700 && calorieDeficitUpdated < 7700) {
				// 更新最近健康紀錄內熱量赤字
				healthRecordService.updateCalorieDeficit(healthRecordLast.getId(),
						healthRecordLast.getCalorieDeficit() + calorieDeficitYestoday);				
			}
		}

	}

	@Scheduled(cron = "00 13 2 * * *") // 指定時間執行 0時(24)
	public void checkDateLine(){
		List<User> userList = userRepository.findAll();
		for (User user : userList) {
			Date dateline = user.getDateline();
			if (dateline == null){
				continue;
			}
			if (dateline.getTime() <= new Date().getTime()){
				if (user.getRole() == "ROLE_DEF"){
					System.out.println("def--->");
					continue;
				}
				System.out.println("過期");
				userRepository.updateUserRole(user.getId(), "ROLE_DEF");
			}
		}
	}
	
	/**
	 * 依體型變化 更新 Avatar
	 */
	@Override
	@Scheduled(cron = "45 01 16 * * ?") // 指定時間執行 2時(am)
	public void upadteAvatarPicForBodyType() {
		
		long now = System.currentTimeMillis();
		int dayMillis = 86400000;

		// 昨天的日期 util
		java.util.Date utilYestoday = new java.util.Date(now - dayMillis);
		// 把日期轉成SQL型態的Date
		java.sql.Date sqlYestoday = new java.sql.Date(utilYestoday.getTime());

		System.out.println(sqlYestoday);
		
		// 取昨日健康紀錄
		List<HealthRecord> healthRecordsYestoday = healthRecordService.findByDate(sqlYestoday);
		
		// 檢查 user Avatar & HealthRecord 的 bodyType 是否一致 
		for(HealthRecord hr : healthRecordsYestoday) {
			
			Avatar userAvatear = hr.getUser().getAvatar();
			
			// 新體型 與 avatar 體型
			BodyType healthRecordBodyType = hr.getBodyType();
			BodyType userAvatarBodyType = 
					userAvatear.getAvatarBody().getBodyType();
			
			// 如果 BodyType 不相等則更新
			if (!healthRecordBodyType.equals(userAvatarBodyType)) {
				
				// 找配件
				String color = userAvatear.getAvatarBody().getColor();
				String clothesName = userAvatear.getAvatarClothes().getName();
				String hatName = userAvatear.getAvatarHat().getName();
				
				// 存取路徑
				String saveFileName = "Avatar_" + hr.getUser().getId(); 
				
				// 更新體型以依最新 healthRecord
				avatarService.saveAvatarPic(
						healthRecordBodyType, color, clothesName, hatName, saveFileName);
			}
		}
	}
}















