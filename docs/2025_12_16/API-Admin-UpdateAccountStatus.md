PUT api/v1/admin/users/{accountId}/status

## Authorization
- Role required: ADMIN
- Header: Authorization: Bearer <admin_token>

## Path Param
- accountId: id của account cần cập nhật trạng thái

## Body
{
  "status": "ACTIVE" | "INACTIVE",
  "reason": "<optional>"
}

## Notes
- Không cho phép cập nhật trạng thái của tài khoản ADMIN (ví dụ: admin_1).
- AccountStatus hiện hỗ trợ: ACTIVE, INACTIVE.

## Seed data (từ postgres_data/init_sql/90_seed_accounts_and_permissions.sql)
- Admin account (KHÔNG được đổi status):
  - accountId: a1000001-0000-0000-0000-000000000003 (username: admin_1)
- Customer account (đang INACTIVE):
  - accountId: a1000001-0000-0000-0000-000000000004 (username: mary_jones)
- Employee account (ACTIVE):
  - accountId: a1000001-0000-0000-0000-000000000018 (username: levann)

## Example 1: Kích hoạt customer đang INACTIVE
PUT api/v1/admin/users/a1000001-0000-0000-0000-000000000004/status

Body:
{
  "status": "ACTIVE",
  "reason": "Kích hoạt lại sau khi xác minh"
}

Output (success):
{
  "success": true,
  "message": "Kích hoạt tài khoản thành công",
  "data": {
    "userType": "CUSTOMER",
    "account": {
      "accountId": "a1000001-0000-0000-0000-000000000004",
      "phoneNumber": "0909876543",
      "status": "ACTIVE",
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
  }
}

## Example 2: Vô hiệu hóa employee
PUT api/v1/admin/users/a1000001-0000-0000-0000-000000000018/status

Body:
{
  "status": "INACTIVE",
  "reason": "Tạm khóa theo yêu cầu"
}

Output (success):
{
  "success": true,
  "message": "Vô hiệu hóa tài khoản thành công",
  "data": {
    "userType": "EMPLOYEE",
    "account": {
      "accountId": "a1000001-0000-0000-0000-000000000018",
      "phoneNumber": "0865222109",
      "status": "INACTIVE",
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
  }
}

## Example 3: Thử đổi status của ADMIN (bị chặn)
PUT api/v1/admin/users/a1000001-0000-0000-0000-000000000003/status

Body:
{
  "status": "INACTIVE",
  "reason": "test"
}

Output (400):
{
  "success": false,
  "message": "Không thể thay đổi trạng thái của tài khoản quản trị viên"
}
