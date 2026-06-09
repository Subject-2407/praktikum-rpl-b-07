<?php
require_once __DIR__ . '/../vendor/autoload.php';
require_once __DIR__ . '/../config/bootstrap.php';

use Scapes\Infrastructure\Database\DatabaseConnection;

$db = DatabaseConnection::getInstance();

$email = 'test@example.com';
$password = 'password123';
$hash = password_hash($password, PASSWORD_BCRYPT);

$db->query(
    "INSERT INTO users (email, password_hash, role, is_verified, created_at, updated_at) VALUES (?, ?, ?, ?, NOW(), NOW())",
    [$email, $hash, 'contributor', 1]
);

echo "User created!\n";
echo "Email   : $email\n";
echo "Password: $password\n";