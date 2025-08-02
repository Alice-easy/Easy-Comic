import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:package_info_plus/package_info_plus.dart';
import 'package:url_launcher/url_launcher.dart';

class AboutSettingsScreen extends StatefulWidget {
  const AboutSettingsScreen({super.key});

  @override
  State<AboutSettingsScreen> createState() => _AboutSettingsScreenState();
}

class _AboutSettingsScreenState extends State<AboutSettingsScreen> {
  PackageInfo? _packageInfo;

  @override
  void initState() {
    super.initState();
    _loadPackageInfo();
  }

  Future<void> _loadPackageInfo() async {
    final info = await PackageInfo.fromPlatform();
    setState(() {
      _packageInfo = info;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('关于'),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            _buildAppInfoSection(context),
            const SizedBox(height: 24),
            const Divider(),
            _buildLinksSection(context),
            const SizedBox(height: 24),
            const Divider(),
            _buildDeveloperInfoSection(context),
            const SizedBox(height: 24),
            _buildCopyrightSection(context),
          ],
        ),
      ),
    );
  }

  Widget _buildAppInfoSection(BuildContext context) {
    return Center(
      child: Column(
        children: [
          Container(
            width: 80,
            height: 80,
            decoration: BoxDecoration(
              color: Theme.of(context).primaryColor,
              borderRadius: BorderRadius.circular(16),
            ),
            child: const Icon(
              Icons.menu_book,
              size: 40,
              color: Colors.white,
            ),
          ),
          const SizedBox(height: 16),
          Text(
            'Easy Comic',
            style: Theme.of(context).textTheme.headlineSmall?.copyWith(
              fontWeight: FontWeight.bold,
            ),
          ),
          const SizedBox(height: 4),
          Text(
            '优雅的漫画阅读器',
            style: Theme.of(context).textTheme.bodyMedium?.copyWith(
              color: Colors.grey[600],
            ),
          ),
          const SizedBox(height: 8),
          if (_packageInfo != null)
            Text(
              '版本 ${_packageInfo!.version} (${_packageInfo!.buildNumber})',
              style: Theme.of(context).textTheme.bodySmall?.copyWith(
                color: Colors.grey[500],
              ),
            ),
        ],
      ),
    );
  }

  Widget _buildLinksSection(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          '链接',
          style: Theme.of(context).textTheme.titleLarge,
        ),
        const SizedBox(height: 8),
        ListTile(
          leading: const Icon(Icons.code),
          title: const Text('源代码'),
          onTap: () => _launchUrl('https://github.com/username/easy-comic'),
        ),
        ListTile(
          leading: const Icon(Icons.bug_report_outlined),
          title: const Text('反馈问题'),
          onTap: () => _launchUrl('https://github.com/username/easy-comic/issues'),
        ),
        ListTile(
          leading: const Icon(Icons.article_outlined),
          title: const Text('开源许可'),
          onTap: () => _showLicenseDialog(context),
        ),
        ListTile(
          leading: const Icon(Icons.privacy_tip_outlined),
          title: const Text('隐私政策'),
          onTap: () => _showPrivacyDialog(context),
        ),
      ],
    );
  }

  Widget _buildDeveloperInfoSection(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          '开发者',
          style: Theme.of(context).textTheme.titleLarge,
        ),
        const SizedBox(height: 8),
        const ListTile(
          leading: Icon(Icons.person_outline),
          title: Text('Easy Comic Team'),
        ),
        ListTile(
          leading: const Icon(Icons.email_outlined),
          title: const Text('developer@easycomic.app'),
          onTap: () => _copyToClipboard('developer@easycomic.app'),
        ),
      ],
    );
  }

  Widget _buildCopyrightSection(BuildContext context) {
    return Center(
      child: Text(
        '© 2024 Easy Comic. All rights reserved.',
        style: Theme.of(context).textTheme.bodySmall?.copyWith(
          color: Colors.grey[500],
        ),
      ),
    );
  }

  Future<void> _launchUrl(String urlString) async {
    final url = Uri.parse(urlString);
    if (await canLaunchUrl(url)) {
      await launchUrl(url, mode: LaunchMode.externalApplication);
    } else {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('无法打开链接：$urlString')),
        );
      }
    }
  }

  void _copyToClipboard(String text) {
    Clipboard.setData(ClipboardData(text: text));
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(content: Text('已复制到剪贴板')),
    );
  }

  void _showLicenseDialog(BuildContext context) {
    // License dialog implementation
  }

  void _showPrivacyDialog(BuildContext context) {
    // Privacy dialog implementation
  }
}