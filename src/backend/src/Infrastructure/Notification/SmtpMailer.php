<?php

/**
 * SMTP mailer sederhana berbasis stream socket.
 *
 * @package Scapes\Infrastructure\Notification
 * @version 1.0
 */

declare(strict_types=1);

namespace Scapes\Infrastructure\Notification;

use Scapes\Core\Exceptions\NotificationException;

/**
 * Kelas SmtpMailer - Mengirim email melalui server SMTP.
 */
class SmtpMailer {

  /**
   * Host SMTP.
   *
   * @var string
   */
  private string $host;

  /**
   * Port SMTP.
   *
   * @var int
   */
  private int $port;

  /**
   * Username SMTP.
   *
   * @var string
   */
  private string $username;

  /**
   * Password SMTP.
   *
   * @var string
   */
  private string $password;

  /**
   * Alamat pengirim.
   *
   * @var string
   */
  private string $fromAddress;

  /**
   * Nama pengirim.
   *
   * @var string
   */
  private string $fromName;

  /**
   * Jenis enkripsi SMTP.
   *
   * @var string
   */
  private string $encryption;

  /**
   * Timeout koneksi dalam detik.
   *
   * @var int
   */
  private int $timeout;

  /**
   * Domain HELO/EHLO.
   *
   * @var string
   */
  private string $heloDomain;

  /**
   * Konstruktor SmtpMailer.
   *
   * @param string $host Host SMTP.
   * @param int $port Port SMTP.
   * @param string $username Username SMTP.
   * @param string $password Password SMTP.
   * @param string $fromAddress Email pengirim.
   * @param string $fromName Nama pengirim.
   * @param string $encryption none, tls, atau ssl.
   * @param int $timeout Timeout koneksi.
   * @param string $heloDomain Domain EHLO.
   */
  public function __construct(
    string $host,
    int $port,
    string $username,
    string $password,
    string $fromAddress,
    string $fromName = 'Scapes',
    string $encryption = 'tls',
    int $timeout = 10,
    string $heloDomain = 'localhost'
  ) {
    $this->host = trim($host);
    $this->port = $port;
    $this->username = trim($username);
    $this->password = $password;
    $this->fromAddress = trim($fromAddress);
    $this->fromName = trim($fromName) !== '' ? trim($fromName) : 'Scapes';
    $this->encryption = strtolower(trim($encryption)) ?: 'tls';
    $this->timeout = max(1, $timeout);
    $this->heloDomain = trim($heloDomain) !== '' ? trim($heloDomain) : 'localhost';

    if (!filter_var($this->fromAddress, FILTER_VALIDATE_EMAIL)) {
      throw new NotificationException('MAIL_FROM_ADDRESS tidak valid.');
    }

    if (!in_array($this->encryption, ['none', 'tls', 'ssl'], true)) {
      throw new NotificationException('SMTP_ENCRYPTION harus none, tls, atau ssl.');
    }
  }

  /**
   * Mengirim email plain text melalui SMTP.
   *
   * @param string $toAddress Email penerima.
   * @param string $subject Subject email.
   * @param string $body Body plain text UTF-8.
   *
   * @return void
   */
  public function send(string $toAddress, string $subject, string $body): void {
    $toAddress = trim($toAddress);
    if (!filter_var($toAddress, FILTER_VALIDATE_EMAIL)) {
      throw new NotificationException('Alamat email penerima tidak valid.');
    }

    $socket = $this->connect();

    try {
      $this->expectResponse($socket, [220]);
      $this->sendCommand($socket, 'EHLO ' . $this->heloDomain, [250]);

      if ($this->encryption === 'tls') {
        $this->sendCommand($socket, 'STARTTLS', [220]);
        $cryptoEnabled = stream_socket_enable_crypto(
          $socket,
          true,
          STREAM_CRYPTO_METHOD_TLS_CLIENT
        );
        if ($cryptoEnabled !== true) {
          throw new NotificationException('Gagal mengaktifkan STARTTLS.');
        }

        $this->sendCommand($socket, 'EHLO ' . $this->heloDomain, [250]);
      }

      if ($this->username !== '') {
        $this->sendCommand($socket, 'AUTH LOGIN', [334]);
        $this->sendCommand($socket, base64_encode($this->username), [334]);
        $this->sendCommand($socket, base64_encode($this->password), [235]);
      }

      $this->sendCommand(
        $socket,
        'MAIL FROM:<' . $this->fromAddress . '>',
        [250]
      );
      $this->sendCommand($socket, 'RCPT TO:<' . $toAddress . '>', [250, 251]);
      $this->sendCommand($socket, 'DATA', [354]);

      $message = $this->buildMessage($toAddress, $subject, $body);
      $this->write($socket, $message . "\r\n.\r\n");
      $this->expectResponse($socket, [250]);

      $this->sendCommand($socket, 'QUIT', [221]);
    } finally {
      if (is_resource($socket)) {
        fclose($socket);
      }
    }
  }

  /**
   * Membuka koneksi socket ke server SMTP.
   *
   * @return resource
   */
  private function connect() {
    $transportHost = $this->encryption === 'ssl'
      ? 'ssl://' . $this->host
      : $this->host;
    $errno = 0;
    $error = '';
    $socket = @stream_socket_client(
      $transportHost . ':' . $this->port,
      $errno,
      $error,
      $this->timeout
    );

    if (!is_resource($socket)) {
      throw new NotificationException(
        'Gagal terhubung ke SMTP server: ' . trim($error) . ' (' . $errno . ').'
      );
    }

    stream_set_timeout($socket, $this->timeout);

    return $socket;
  }

  /**
   * Mengirim command SMTP dan memvalidasi response.
   *
   * @param resource $socket Socket SMTP.
   * @param string $command Command SMTP.
   * @param array<int, int> $expectedCodes Kode response yang diterima.
   *
   * @return void
   */
  private function sendCommand($socket, string $command, array $expectedCodes): void {
    $this->write($socket, $command . "\r\n");
    $this->expectResponse($socket, $expectedCodes);
  }

  /**
   * Menulis data ke socket SMTP.
   *
   * @param resource $socket Socket SMTP.
   * @param string $payload Payload yang dikirim.
   *
   * @return void
   */
  private function write($socket, string $payload): void {
    $written = @fwrite($socket, $payload);
    if ($written === false || $written < strlen($payload)) {
      throw new NotificationException('Gagal menulis data ke SMTP socket.');
    }
  }

  /**
   * Memastikan response SMTP sesuai kode yang diharapkan.
   *
   * @param resource $socket Socket SMTP.
   * @param array<int, int> $expectedCodes Kode yang diharapkan.
   *
   * @return void
   */
  private function expectResponse($socket, array $expectedCodes): void {
    [$code, $message] = $this->readResponse($socket);
    if (!in_array($code, $expectedCodes, true)) {
      throw new NotificationException(
        'SMTP server mengembalikan response tidak valid: '
        . $code
        . ' '
        . $message
      );
    }
  }

  /**
   * Membaca response SMTP yang bisa multiline.
   *
   * @param resource $socket Socket SMTP.
   *
   * @return array{0: int, 1: string}
   */
  private function readResponse($socket): array {
    $lines = [];

    while (($line = fgets($socket, 515)) !== false) {
      $lines[] = rtrim($line, "\r\n");
      if (strlen($line) >= 4 && $line[3] === ' ') {
        break;
      }
    }

    if ($lines === []) {
      throw new NotificationException('Tidak ada response dari SMTP server.');
    }

    $lastLine = $lines[count($lines) - 1];
    $code = (int) substr($lastLine, 0, 3);

    return [$code, implode(' ', $lines)];
  }

  /**
   * Membentuk MIME message sederhana.
   *
   * @param string $toAddress Email penerima.
   * @param string $subject Subject email.
   * @param string $body Body email.
   *
   * @return string
   */
  private function buildMessage(string $toAddress, string $subject, string $body): string {
    $safeSubject = str_replace(["\r", "\n"], '', trim($subject));
    $normalizedBody = str_replace(["\r\n", "\r"], "\n", $body);
    $normalizedBody = implode(
      "\r\n",
      array_map(
        static fn (string $line): string => str_starts_with($line, '.') ? '.' . $line : $line,
        explode("\n", $normalizedBody)
      )
    );

    $headers = [
      'Date: ' . gmdate('D, d M Y H:i:s O'),
      'From: ' . $this->formatAddress($this->fromAddress, $this->fromName),
      'To: <' . $toAddress . '>',
      'Subject: ' . $safeSubject,
      'MIME-Version: 1.0',
      'Content-Type: text/plain; charset=UTF-8',
      'Content-Transfer-Encoding: 8bit',
    ];

    return implode("\r\n", $headers) . "\r\n\r\n" . $normalizedBody;
  }

  /**
   * Membentuk header alamat email.
   *
   * @param string $address Email.
   * @param string $name Nama tampilan.
   *
   * @return string
   */
  private function formatAddress(string $address, string $name): string {
    $safeName = str_replace(['"', "\r", "\n"], ['', '', ''], $name);
    return '"' . $safeName . '" <' . $address . '>';
  }
}
