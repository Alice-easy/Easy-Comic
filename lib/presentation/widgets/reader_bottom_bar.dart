import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import '../features/reader/bloc/reader_bloc.dart';
import '../features/reader/bloc/reader_event.dart';

class ReaderBottomBar extends StatelessWidget {
  final int currentPage;
  final int totalPages;
  final Function(int) onSliderChanged;

  const ReaderBottomBar({
    super.key,
    required this.currentPage,
    required this.totalPages,
    required this.onSliderChanged,
  });

  @override
  Widget build(BuildContext context) {
    return BottomAppBar(
      child: Row(
        children: [
          IconButton(
            icon: const Icon(Icons.arrow_back),
            onPressed: () {
              context.read<ReaderBloc>().add(PreviousPage());
            },
          ),
          Expanded(
            child: Slider(
              value: currentPage.toDouble(),
              min: 0,
              max: totalPages > 0 ? (totalPages - 1).toDouble() : 0,
              divisions: totalPages > 1 ? totalPages - 1 : null,
              onChanged: (value) {
                // We can provide live feedback here if needed
              },
              onChangeEnd: (value) {
                onSliderChanged(value.toInt());
              },
            ),
          ),
          IconButton(
            icon: const Icon(Icons.arrow_forward),
            onPressed: () {
              context.read<ReaderBloc>().add(NextPage());
            },
          ),
        ],
      ),
    );
  }
}