/// 自动翻页配置
class AutoPageConfig {
  final bool enabled;
  final Duration interval;
  final bool pauseOnInteraction;
  final bool pauseOnFocusLoss;
  final bool enableSoundFeedback;
  final bool enableVisualIndicator;
  final double autoScrollSpeed;
  final bool reverseOnReachEnd;
  
  // 新增属性
  final bool pauseOnUserInteraction;
  final Duration interactionPauseDelay;
  final int defaultInterval;
  final bool stopAtLastPage;
  final bool pauseOnAppBackground;

  const AutoPageConfig({
    this.enabled = false,
    this.interval = const Duration(seconds: 3),
    this.pauseOnInteraction = true,
    this.pauseOnFocusLoss = true,
    this.enableSoundFeedback = false,
    this.enableVisualIndicator = true,
    this.autoScrollSpeed = 1.0,
    this.reverseOnReachEnd = false,
    this.pauseOnUserInteraction = true,
    this.interactionPauseDelay = const Duration(seconds: 3),
    this.defaultInterval = 5,
    this.stopAtLastPage = false,
    this.pauseOnAppBackground = true,
  });

  AutoPageConfig copyWith({
    bool? enabled,
    Duration? interval,
    bool? pauseOnInteraction,
    bool? pauseOnFocusLoss,
    bool? enableSoundFeedback,
    bool? enableVisualIndicator,
    double? autoScrollSpeed,
    bool? reverseOnReachEnd,
    bool? pauseOnUserInteraction,
    Duration? interactionPauseDelay,
    int? defaultInterval,
    bool? stopAtLastPage,
    bool? pauseOnAppBackground,
  }) {
    return AutoPageConfig(
      enabled: enabled ?? this.enabled,
      interval: interval ?? this.interval,
      pauseOnInteraction: pauseOnInteraction ?? this.pauseOnInteraction,
      pauseOnFocusLoss: pauseOnFocusLoss ?? this.pauseOnFocusLoss,
      enableSoundFeedback: enableSoundFeedback ?? this.enableSoundFeedback,
      enableVisualIndicator: enableVisualIndicator ?? this.enableVisualIndicator,
      autoScrollSpeed: autoScrollSpeed ?? this.autoScrollSpeed,
      reverseOnReachEnd: reverseOnReachEnd ?? this.reverseOnReachEnd,
      pauseOnUserInteraction: pauseOnUserInteraction ?? this.pauseOnUserInteraction,
      interactionPauseDelay: interactionPauseDelay ?? this.interactionPauseDelay,
      defaultInterval: defaultInterval ?? this.defaultInterval,
      stopAtLastPage: stopAtLastPage ?? this.stopAtLastPage,
      pauseOnAppBackground: pauseOnAppBackground ?? this.pauseOnAppBackground,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'enabled': enabled,
      'interval': interval.inMilliseconds,
      'pauseOnInteraction': pauseOnInteraction,
      'pauseOnFocusLoss': pauseOnFocusLoss,
      'enableSoundFeedback': enableSoundFeedback,
      'enableVisualIndicator': enableVisualIndicator,
      'autoScrollSpeed': autoScrollSpeed,
      'reverseOnReachEnd': reverseOnReachEnd,
      'pauseOnUserInteraction': pauseOnUserInteraction,
      'interactionPauseDelayMs': interactionPauseDelay.inMilliseconds,
      'defaultInterval': defaultInterval,
      'stopAtLastPage': stopAtLastPage,
      'pauseOnAppBackground': pauseOnAppBackground,
    };
  }

  factory AutoPageConfig.fromJson(Map<String, dynamic> json) {
    return AutoPageConfig(
      enabled: json['enabled'] ?? false,
      interval: Duration(milliseconds: json['interval'] ?? 3000),
      pauseOnInteraction: json['pauseOnInteraction'] ?? true,
      pauseOnFocusLoss: json['pauseOnFocusLoss'] ?? true,
      enableSoundFeedback: json['enableSoundFeedback'] ?? false,
      enableVisualIndicator: json['enableVisualIndicator'] ?? true,
      autoScrollSpeed: json['autoScrollSpeed']?.toDouble() ?? 1.0,
      reverseOnReachEnd: json['reverseOnReachEnd'] ?? false,
      pauseOnUserInteraction: json['pauseOnUserInteraction'] ?? true,
      interactionPauseDelay: Duration(milliseconds: json['interactionPauseDelayMs'] ?? 3000),
      defaultInterval: json['defaultInterval'] ?? 5,
      stopAtLastPage: json['stopAtLastPage'] ?? false,
      pauseOnAppBackground: json['pauseOnAppBackground'] ?? true,
    );
  }

  @override
  bool operator ==(Object other) {
    if (identical(this, other)) return true;
    return other is AutoPageConfig &&
        other.enabled == enabled &&
        other.interval == interval &&
        other.pauseOnInteraction == pauseOnInteraction &&
        other.pauseOnFocusLoss == pauseOnFocusLoss &&
        other.enableSoundFeedback == enableSoundFeedback &&
        other.enableVisualIndicator == enableVisualIndicator &&
        other.autoScrollSpeed == autoScrollSpeed &&
        other.reverseOnReachEnd == reverseOnReachEnd &&
        other.pauseOnUserInteraction == pauseOnUserInteraction &&
        other.interactionPauseDelay == interactionPauseDelay &&
        other.defaultInterval == defaultInterval &&
        other.stopAtLastPage == stopAtLastPage &&
        other.pauseOnAppBackground == pauseOnAppBackground;
  }

  @override
  int get hashCode {
    return enabled.hashCode ^
        interval.hashCode ^
        pauseOnInteraction.hashCode ^
        pauseOnFocusLoss.hashCode ^
        enableSoundFeedback.hashCode ^
        enableVisualIndicator.hashCode ^
        autoScrollSpeed.hashCode ^
        reverseOnReachEnd.hashCode ^
        pauseOnUserInteraction.hashCode ^
        interactionPauseDelay.hashCode ^
        defaultInterval.hashCode ^
        stopAtLastPage.hashCode ^
        pauseOnAppBackground.hashCode;
  }

  @override
  String toString() {
    return 'AutoPageConfig(enabled: $enabled, interval: $interval, pauseOnInteraction: $pauseOnInteraction, pauseOnFocusLoss: $pauseOnFocusLoss, enableSoundFeedback: $enableSoundFeedback, enableVisualIndicator: $enableVisualIndicator, autoScrollSpeed: $autoScrollSpeed, reverseOnReachEnd: $reverseOnReachEnd, pauseOnUserInteraction: $pauseOnUserInteraction, interactionPauseDelay: $interactionPauseDelay, defaultInterval: $defaultInterval, stopAtLastPage: $stopAtLastPage, pauseOnAppBackground: $pauseOnAppBackground)';
  }
}