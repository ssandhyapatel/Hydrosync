import pandas as pd
import seaborn as sns
import matplotlib.pyplot as plt

# Load your dataset
df = pd.read_csv("HydroSync_Health_Data.csv")

# Sample for faster plotting
sample_df = df.sample(n=min(10000, len(df)), random_state=42)


# Plot style
sns.set(style="whitegrid")
fig, axes = plt.subplots(2, 2, figsize=(16, 12))

# 1. Hydration vs Fatigue
sns.scatterplot(data=sample_df, x='HydrationLevel(%)', y='FatigueScore(0-100)', alpha=0.4, ax=axes[0, 0])
axes[0, 0].set_title('Hydration vs Fatigue Score')

# 2. Sleep Hours by Activity Level
sns.boxplot(data=sample_df, x='ActivityLevel', y='SleepHours', ax=axes[0, 1])
axes[0, 1].set_title('Sleep Hours by Activity Level')

# 3. Heart Rate by Gender
sns.violinplot(data=sample_df, x='Gender', y='HeartRate', ax=axes[1, 0])
axes[1, 0].set_title('Heart Rate Distribution by Gender')

# 4. Hydration by Age Group
age_bins = pd.cut(sample_df['Age'], bins=[15, 25, 35, 45, 55, 65], labels=['16-25', '26-35', '36-45', '46-55', '56-65'])
age_grouped = sample_df.groupby(age_bins)['HydrationLevel(%)'].mean().reset_index()
sns.barplot(data=age_grouped, x='Age', y='HydrationLevel(%)', ax=axes[1, 1])
axes[1, 1].set_title('Average Hydration by Age Group')

plt.tight_layout()
plt.show()
