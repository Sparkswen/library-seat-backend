package com.jlau.libraryseat;

import com.jlau.libraryseat.entity.Seat;
import com.jlau.libraryseat.entity.User;
import com.jlau.libraryseat.repository.SeatRepository;
import com.jlau.libraryseat.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class LibrarySeatApplication {

	public static void main(String[] args) {
		SpringApplication.run(LibrarySeatApplication.class, args);
	}

	@Bean
	CommandLineRunner init(UserRepository userRepo, SeatRepository seatRepo) {
		return args -> {
			// 创建测试学生
			if (!userRepo.existsByStudentNo("2022001")) {
				User user = new User();
				user.setStudentNo("2022001");
				user.setName("测试学生");
				user.setPassword("123456");
				user.setRole(User.Role.STUDENT);
				userRepo.save(user);
				System.out.println("✅ 测试学生：2022001 / 123456");
			}

			// 创建管理员
			if (!userRepo.existsByStudentNo("admin")) {
				User admin = new User();
				admin.setStudentNo("admin");
				admin.setName("系统管理员");
				admin.setPassword("admin123");
				admin.setRole(User.Role.ADMIN);
				userRepo.save(admin);
				System.out.println("✅ 管理员账号：admin / admin123");
			}

			// 创建馆员
			if (!userRepo.existsByStudentNo("librarian")) {
				User librarian = new User();
				librarian.setStudentNo("librarian");
				librarian.setName("图书管理员");
				librarian.setPassword("lib123");
				librarian.setRole(User.Role.LIBRARIAN);
				userRepo.save(librarian);
				System.out.println("✅ 馆员账号：librarian / lib123");
			}

			// 创建座位
			if (seatRepo.count() == 0) {
				String[] areas = {"A区", "B区", "C区"};
				for (int floor = 1; floor <= 3; floor++) {
					for (String area : areas) {
						for (int i = 1; i <= 10; i++) {
							Seat seat = new Seat();
							seat.setFloor(floor);
							seat.setArea(area);
							seat.setSeatNo(floor + "F-" + area + "-" + String.format("%02d", i));
							seatRepo.save(seat);
						}
					}
				}
				System.out.println("✅ 创建了90个座位");
			}
		};
	}
}
