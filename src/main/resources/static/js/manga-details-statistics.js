document.addEventListener('DOMContentLoaded', function() {
    const mangaStatsChartCtx = document.getElementById('mangaStatsChart').getContext('2d');
    const mangaStatsGradeCtx = document.getElementById('mangaStatsGrade').getContext('2d');

    const countReading = window.countReading || 0;
    const countWantToRead = window.countWantToRead || 0;
    const countStoppedReading = window.countStoppedReading || 0;
    const countRecited = window.countRecited || 0;
    const countFavorites = window.countFavorites || 0;

    const totalCount = countReading + countWantToRead + countStoppedReading + countRecited + countFavorites;

    const mangaStatsChartData = {
        labels: [
            'Читають',
            'Хочуть прочитати',
            'Припинили читати',
            'Прочитали',
            'Улюблені'
        ],
        datasets: [{
            label: 'Списки',
            data: [countReading, countWantToRead, countStoppedReading, countRecited, countFavorites],
            backgroundColor: '#FF0080',
            borderColor: '#E80074',
            borderWidth: 1,
            barThickness: 20,
            borderRadius: 10,
            borderSkipped: false
        }]
    };

    const mangaStatsChartConfig = {
        type: 'bar',
        data: mangaStatsChartData,
        options: {
            responsive: true,
            indexAxis: 'y',
            plugins: {
                legend: {
                    display: true,
                    position: 'top'
                },
                datalabels: {
                    display: true,
                    anchor: 'end',
                    align: 'right',
                    offset: 4,
                    formatter: (value) => {
                        const percentage = totalCount === 0 ? '0%' : ((value / totalCount) * 100).toFixed(1) + '%';
                        return `${value} (${percentage})`;
                    },
                    color: '#fff',
                    font: {
                        weight: 'bold',
                        size: 11
                    }
                }
            },
            scales: {
                x: {
                    beginAtZero: true,
                    max: totalCount,
                    grid: {
                        display: false
                    },
                    ticks: {
                        display: false,
                    },
                    border: {
                        display: false
                    }
                },
                y: {
                    grid: {
                        display: false
                    },
                    ticks: {
                        display: true
                    },
                    border: {
                        display: false
                    }
                }
            },
            layout: {
                padding: {
                    right: 50
                }
            }
        },
        plugins: [ChartDataLabels]
    };

    const countOneStar = window.countOneStar || 0;
    const countTwoStar = window.countTwoStar || 0;
    const countThreeStar = window.countThreeStar || 0;
    const countFourStar = window.countFourStar || 0;
    const countFiveStar = window.countFiveStar || 0;

    const totalCountGrade = countOneStar + countTwoStar + countThreeStar + countFourStar + countFiveStar;

    const mangaStatsGradeData = {
        labels: ['1 зірка', '2 зірки', '3 зірки', '4 зірки', '5 зірок'],
        datasets: [{
            label: 'Оцінки',
            data: [countOneStar, countTwoStar, countThreeStar, countFourStar, countFiveStar],
            backgroundColor: '#FF0080',
            borderColor: '#E80074',
            borderWidth: 1,
            barThickness: 20,
            borderRadius: 10,
            borderSkipped: false
        }]
    };

    const mangaStatsGradeConfig = {
        type: 'bar',
        data: mangaStatsGradeData,
        options: {
            responsive: true,
            indexAxis: 'y',
            plugins: {
                legend: {
                    display: true,
                    position: 'top'
                },
                datalabels: {
                    display: true,
                    anchor: 'end',
                    align: 'right',
                    offset: 4,
                    formatter: (value) => {
                        const percentage = totalCountGrade === 0 ? '0%' : ((value / totalCountGrade) * 100).toFixed(1) + '%';
                        return `${value} (${percentage})`;
                    },
                    color: '#fff',
                    font: {
                        weight: 'bold',
                        size: 11
                    }
                }
            },
            scales: {
                x: {
                    beginAtZero: true,
                    max: totalCountGrade,
                    grid: {
                        display: false
                    },
                    ticks: {
                        display: false,
                    },
                    border: {
                        display: false
                    }
                },
                y: {
                    grid: {
                        display: false
                    },
                    ticks: {
                        display: true
                    },
                    border: {
                        display: false
                    }
                }
            },
            layout: {
                padding: {
                    right: 80
                }
            }
        },
        plugins: [ChartDataLabels]
    };

    new Chart(mangaStatsChartCtx, mangaStatsChartConfig);
    new Chart(mangaStatsGradeCtx, mangaStatsGradeConfig);
});