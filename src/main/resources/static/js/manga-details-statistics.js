document.addEventListener('DOMContentLoaded', function() {
    const listsData = [
        { label: 'Читають', value: window.countReading },
        { label: 'У планах', value: window.countWantToRead },
        { label: 'У кинутих', value: window.countStoppedReading },
        { label: 'Прочитано', value: window.countRecited },
        { label: 'Улюблені', value: window.countFavorites }
    ];

    const gradesData = [
        { label: '1 зірка', value: window.countOneStar },
        { label: '2 зірки', value: window.countTwoStar },
        { label: '3 зірки', value: window.countThreeStar },
        { label: '4 зірки', value: window.countFourStar },
        { label: '5 зірок', value: window.countFiveStar }
    ];

    function createChart(containerId, data) {
        const container = document.getElementById(containerId);
        const totalValue = data.reduce((acc, item) => acc + item.value, 0);

        data.forEach(item => {
            const barWrapper = document.createElement('div');
            barWrapper.className = 'bar-wrapper';

            const bar = document.createElement('div');
            bar.className = 'bar';

            const barFill = document.createElement('div');
            barFill.className = 'bar-fill';
            const percentage = totalValue > 0 ? (item.value / totalValue) * 100 : 0;
            barFill.style.width = `${percentage}%`;

            const barLabel = document.createElement('div');
            barLabel.className = 'bar-label';
            barLabel.textContent = item.label;

            const barValue = document.createElement('div');
            barValue.className = 'bar-value';
            barValue.textContent = `${item.value} (${percentage.toFixed(1)}%)`;

            bar.appendChild(barFill);
            barWrapper.appendChild(bar);
            barWrapper.appendChild(barLabel);
            barWrapper.appendChild(barValue);
            container.appendChild(barWrapper);
        });
    }

    createChart('chart-containerLists', listsData);
    createChart('chart-containerGrades', gradesData);
});