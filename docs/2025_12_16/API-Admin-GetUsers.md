GET api/v1/admin/users
## Query Params (optional):
- userType: CUSTOMER, EMPLOYEE and ALL (default = ALL).
- status: ACTIVE/INACTIVE
- page: default=0
- size: default=10

**Output**:
{
    "totalPages": 14,
    "totalItems": 67,
    "currentPage": 0,
    "success": true,
    "data": [
        {
            "userType": "CUSTOMER",
            "account": {
                "accountId": "a1000001-0000-0000-0000-000000000004",
                "phoneNumber": "0909876543",
                "status": "INACTIVE",
                "isPhoneVerified": false,
                "lastLogin": null,
                "roles": [
                    "CUSTOMER"
                ]
            },
            "profile": {
                "id": "c1000001-0000-0000-0000-000000000002",
                "avatar": "https://picsum.photos/200",
                "fullName": "Mary Jones",
                "isMale": false,
                "email": "mary.jones@example.com",
                "isEmailVerified": true,
                "birthdate": "2003-01-19",
                "rating": null,
                "hiredDate": null,
                "skills": null,
                "bio": null,
                "employeeStatus": null,
                "vipLevel": null
            }
        },
        {
            "userType": "CUSTOMER",
            "account": {
                "accountId": "a1000001-0000-0000-0000-000000000006",
                "phoneNumber": "0987654321",
                "status": "ACTIVE",
                "isPhoneVerified": true,
                "lastLogin": null,
                "roles": [
                    "CUSTOMER"
                ]
            },
            "profile": {
                "id": "c1000001-0000-0000-0000-000000000004",
                "avatar": "https://i.pravatar.cc/150?img=11",
                "fullName": "Nguyễn Văn An",
                "isMale": true,
                "email": "nguyenvanan@gmail.com",
                "isEmailVerified": true,
                "birthdate": "1995-03-15",
                "rating": null,
                "hiredDate": null,
                "skills": null,
                "bio": null,
                "employeeStatus": null,
                "vipLevel": null
            }
        },
        {
            "userType": "CUSTOMER",
            "account": {
                "accountId": "a1000001-0000-0000-0000-000000000008",
                "phoneNumber": "0965432109",
                "status": "ACTIVE",
                "isPhoneVerified": true,
                "lastLogin": null,
                "roles": [
                    "CUSTOMER"
                ]
            },
            "profile": {
                "id": "c1000001-0000-0000-0000-000000000006",
                "avatar": "https://i.pravatar.cc/150?img=12",
                "fullName": "Lê Văn Cường",
                "isMale": true,
                "email": "levancuong@gmail.com",
                "isEmailVerified": true,
                "birthdate": "1992-11-08",
                "rating": null,
                "hiredDate": null,
                "skills": null,
                "bio": null,
                "employeeStatus": null,
                "vipLevel": null
            }
        },
        {
            "userType": "CUSTOMER",
            "account": {
                "accountId": "a1000001-0000-0000-0000-000000000009",
                "phoneNumber": "0954321098",
                "status": "ACTIVE",
                "isPhoneVerified": true,
                "lastLogin": null,
                "roles": [
                    "CUSTOMER"
                ]
            },
            "profile": {
                "id": "c1000001-0000-0000-0000-000000000007",
                "avatar": "https://i.pravatar.cc/150?img=9",
                "fullName": "Phạm Thị Dung",
                "isMale": false,
                "email": "phamthidung@gmail.com",
                "isEmailVerified": true,
                "birthdate": "1996-05-30",
                "rating": null,
                "hiredDate": null,
                "skills": null,
                "bio": null,
                "employeeStatus": null,
                "vipLevel": null
            }
        },
        {
            "userType": "CUSTOMER",
            "account": {
                "accountId": "a1000001-0000-0000-0000-000000000010",
                "phoneNumber": "0943210987",
                "status": "ACTIVE",
                "isPhoneVerified": true,
                "lastLogin": null,
                "roles": [
                    "CUSTOMER"
                ]
            },
            "profile": {
                "id": "c1000001-0000-0000-0000-000000000008",
                "avatar": "https://i.pravatar.cc/150?img=13",
                "fullName": "Hoàng Văn Em",
                "isMale": true,
                "email": "hoangvanem@gmail.com",
                "isEmailVerified": true,
                "birthdate": "1994-09-12",
                "rating": null,
                "hiredDate": null,
                "skills": null,
                "bio": null,
                "employeeStatus": null,
                "vipLevel": null
            }
        },
        {
            "userType": "EMPLOYEE",
            "account": {
                "accountId": "a1000001-0000-0000-0000-000000000027",
                "phoneNumber": "0977888999",
                "status": "ACTIVE",
                "isPhoneVerified": true,
                "lastLogin": null,
                "roles": [
                    "EMPLOYEE",
                    "CUSTOMER"
                ]
            },
            "profile": {
                "id": "e1000001-0000-0000-0000-000000000014",
                "avatar": "https://i.pravatar.cc/150?img=47",
                "fullName": "Lê Thị Hương",
                "isMale": false,
                "email": "lethihuong.employee@gmail.com",
                "isEmailVerified": true,
                "birthdate": "1996-12-05",
                "rating": "HIGH",
                "hiredDate": "2023-08-20",
                "skills": [
                    "Chăm sóc trẻ em",
                    "Nấu ăn dinh dưỡng",
                    "Dọn dẹp nhà cửa"
                ],
                "bio": "Kinh nghiệm chăm sóc trẻ nhỏ và nấu ăn dinh dưỡng cho gia đình.",
                "employeeStatus": "BUSY",
                "vipLevel": null
            }
        },
        {
            "userType": "EMPLOYEE",
            "account": {
                "accountId": "a1000001-0000-0000-0000-000000000042",
                "phoneNumber": "0912000013",
                "status": "ACTIVE",
                "isPhoneVerified": true,
                "lastLogin": null,
                "roles": [
                    "EMPLOYEE"
                ]
            },
            "profile": {
                "id": "e1000001-0000-0000-0000-000000000029",
                "avatar": "https://i.pravatar.cc/150?img=57",
                "fullName": "Lê Tùng Nam",
                "isMale": true,
                "email": "levann1@gmail.com",
                "isEmailVerified": true,
                "birthdate": "1989-01-20",
                "rating": "HIGHEST",
                "hiredDate": "2024-02-10",
                "skills": [
                    "Vệ sinh công nghiệp",
                    "Làm sạch kính"
                ],
                "bio": "Chuyên nghiệp, an toàn cao.",
                "employeeStatus": "AVAILABLE",
                "vipLevel": null
            }
        },
        {
            "userType": "EMPLOYEE",
            "account": {
                "accountId": "a1000001-0000-0000-0000-000000000018",
                "phoneNumber": "0865222109",
                "status": "ACTIVE",
                "isPhoneVerified": true,
                "lastLogin": null,
                "roles": [
                    "EMPLOYEE"
                ]
            },
            "profile": {
                "id": "e1000001-0000-0000-0000-000000000005",
                "avatar": "https://i.pravatar.cc/150?img=34",
                "fullName": "Lê Văn Nam",
                "isMale": true,
                "email": "levannam@gmail.com",
                "isEmailVerified": true,
                "birthdate": "1992-11-18",
                "rating": "HIGHEST",
                "hiredDate": "2022-08-20",
                "skills": [
                    "Vệ sinh máy lạnh",
                    "Bảo trì điện",
                    "Sửa chữa nhỏ"
                ],
                "bio": "Có kỹ năng kỹ thuật, chuyên vệ sinh và bảo trì máy lạnh.",
                "employeeStatus": "BUSY",
                "vipLevel": null
            }
        },
        {
            "userType": "EMPLOYEE",
            "account": {
                "accountId": "a1000001-0000-0000-0000-000000000028",
                "phoneNumber": "0966555444",
                "status": "ACTIVE",
                "isPhoneVerified": true,
                "lastLogin": null,
                "roles": [
                    "EMPLOYEE",
                    "CUSTOMER"
                ]
            },
            "profile": {
                "id": "e1000001-0000-0000-0000-000000000015",
                "avatar": "https://i.pravatar.cc/150?img=17",
                "fullName": "Phạm Văn Tuấn",
                "isMale": true,
                "email": "phamvantuan.employee@gmail.com",
                "isEmailVerified": true,
                "birthdate": "1994-06-18",
                "rating": "HIGHEST",
                "hiredDate": "2023-05-10",
                "skills": [
                    "Sửa chữa điện nước",
                    "Vệ sinh máy lạnh",
                    "Bảo trì thiết bị"
                ],
                "bio": "Thợ điện nước lành nghề, chuyên sửa chữa và bảo trì thiết bị gia đình.",
                "employeeStatus": "AVAILABLE",
                "vipLevel": null
            }
        },
        {
            "userType": "EMPLOYEE",
            "account": {
                "accountId": "a1000001-0000-0000-0000-000000000063",
                "phoneNumber": "0912100004",
                "status": "ACTIVE",
                "isPhoneVerified": true,
                "lastLogin": null,
                "roles": [
                    "EMPLOYEE"
                ]
            },
            "profile": {
                "id": "e1000001-0000-0000-0000-000000000050",
                "avatar": "https://i.pravatar.cc/150?img=70",
                "fullName": "Phạm Thị Lan Anh",
                "isMale": false,
                "email": "phamthilananh.govap@gmail.com",
                "isEmailVerified": true,
                "birthdate": "1997-03-30",
                "rating": "HIGH",
                "hiredDate": "2024-03-05",
                "skills": [
                    "Vệ sinh sofa",
                    "Giặt thảm",
                    "Khử khuẩn"
                ],
                "bio": "Chuyên vệ sinh nội thất, sofa, thảm bằng máy móc chuyên dụng tại Gò Vấp.",
                "employeeStatus": "AVAILABLE",
                "vipLevel": null
            }
        }
    ]
}